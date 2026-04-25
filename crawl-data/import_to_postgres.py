#!/usr/bin/env python3
"""
Script import dữ liệu từ các file JSON (word/kanji/grammar) vào PostgreSQL.

Cách dùng:
    python import_to_postgres.py [--data-dir <thư mục chứa json>] [--dry-run]

Yêu cầu:
    pip install psycopg2-binary

Cấu hình kết nối DB: chỉnh sửa biến DB_CONFIG bên dưới.
"""

import json
import os
import sys
import glob
import uuid
import argparse
import logging
from pathlib import Path
from typing import Optional

try:
    import psycopg2
    from psycopg2.extras import execute_values
except ImportError:
    print("Lỗi: Chưa cài psycopg2. Hãy chạy: pip install psycopg2-binary")
    sys.exit(1)

# ─────────────────────────────────────────────────────────
#  CẤU HÌNH KẾT NỐI DATABASE – chỉnh sửa tại đây
# ─────────────────────────────────────────────────────────
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "datn",   # <-- đổi thành tên DB của bạn
    "user": "postgres",           # <-- đổi thành username
    "password": "123456",       # <-- đổi thành password
}

# ─────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger(__name__)


# ══════════════════════════════════════════════════════════
#  HELPERS
# ══════════════════════════════════════════════════════════

def normalize_jlpt(level: Optional[str]) -> Optional[str]:
    """Chuẩn hóa JLPT level về đúng enum: N5, N4, N3."""
    if level is None:
        return None
    level = str(level).strip().upper()
    if level in ("N5", "N4", "N3"):
        return level
    return None


def load_json_file(filepath: str) -> list:
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


# ══════════════════════════════════════════════════════════
#  CORE IMPORT LOGIC
# ══════════════════════════════════════════════════════════

class Importer:
    def __init__(self, conn, dry_run: bool = False):
        self.conn = conn
        self.dry_run = dry_run
        self.stats = {
            "entries": 0,
            "examples": 0,
            "relations": 0,
            "skipped": 0,
        }

    # ── Tra cứu / cache entry đã tồn tại ──────────────────
    def _find_entry_id(self, cur, text: str, entry_type: str) -> Optional[str]:
        """Tìm UUID của entry đã có trong DB theo (text, entry_type)."""
        cur.execute(
            "SELECT id FROM dictionary_entries WHERE text = %s AND entry_type = %s",
            (text, entry_type),
        )
        row = cur.fetchone()
        return str(row[0]) if row else None

    # ── Upsert một dictionary_entry ────────────────────────
    def _upsert_entry(self, cur, meta: dict) -> str:
        """
        Insert entry nếu chưa tồn tại (theo text + entry_type),
        hoặc update nếu đã có. Trả về UUID của entry.
        """
        entry_type = meta["entry_type"]
        text = meta["text"]
        reading = meta.get("reading")
        meaning_vn = meta.get("meaning_vn", "")
        jlpt_level = normalize_jlpt(meta.get("jlpt_level"))
        explanation_short = meta.get("explanation_short")

        existing_id = self._find_entry_id(cur, text, entry_type)

        if existing_id:
            # Update
            cur.execute(
                """
                UPDATE dictionary_entries SET
                    reading = %s,
                    meaning_vn = %s,
                    jlpt_level = %s,
                    explanation_short = %s
                WHERE id = %s
                """,
                (reading, meaning_vn, jlpt_level, explanation_short, existing_id),
            )
            log.debug("  [UPDATE] %s '%s'", entry_type, text)
            return existing_id
        else:
            # Insert
            new_id = str(uuid.uuid4())
            cur.execute(
                """
                INSERT INTO dictionary_entries
                    (id, entry_type, text, reading, meaning_vn, jlpt_level, explanation_short)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                """,
                (new_id, entry_type, text, reading, meaning_vn, jlpt_level, explanation_short),
            )
            log.debug("  [INSERT] %s '%s' -> %s", entry_type, text, new_id)
            self.stats["entries"] += 1
            return new_id

    # ── Insert examples ────────────────────────────────────
    def _insert_examples(self, cur, entry_id: str, examples: list):
        if not examples:
            return
        # Xóa examples cũ rồi insert lại (tránh duplicate)
        cur.execute("DELETE FROM examples WHERE entry_id = %s", (entry_id,))
        rows = [
            (str(uuid.uuid4()), entry_id, ex["japanese_sentence"], ex["vietnamese_sentence"])
            for ex in examples
            if ex.get("japanese_sentence") and ex.get("vietnamese_sentence")
        ]
        if rows:
            execute_values(
                cur,
                "INSERT INTO examples (id, entry_id, japanese_sentence, vietnamese_sentence) VALUES %s",
                rows,
            )
            self.stats["examples"] += len(rows)

    # ── Insert entry_relations (synonym/antonym/compound) ──
    def _insert_relation(self, cur, source_id: str, target_text: str, relation_type: str, entry_type: str):
        """
        Tìm target entry theo text; nếu chưa có thì tạo stub entry,
        rồi insert vào entry_relations.
        """
        target_id = self._find_entry_id(cur, target_text, entry_type)
        if not target_id:
            # Tạo stub entry (chưa có đủ thông tin)
            target_id = str(uuid.uuid4())
            cur.execute(
                """
                INSERT INTO dictionary_entries (id, entry_type, text, meaning_vn)
                VALUES (%s, %s, %s, %s)
                ON CONFLICT DO NOTHING
                """,
                (target_id, entry_type, target_text, ""),
            )
            # Nếu ON CONFLICT xảy ra, cần lấy lại id
            existing = self._find_entry_id(cur, target_text, entry_type)
            if existing:
                target_id = existing

        # Kiểm tra relation đã tồn tại chưa
        cur.execute(
            """
            SELECT id FROM entry_relations
            WHERE source_id = %s AND target_id = %s AND relation_type = %s
            """,
            (source_id, target_id, relation_type),
        )
        if not cur.fetchone():
            cur.execute(
                """
                INSERT INTO entry_relations (id, source_id, target_id, relation_type)
                VALUES (%s, %s, %s, %s)
                """,
                (str(uuid.uuid4()), source_id, target_id, relation_type),
            )
            self.stats["relations"] += 1

    # ── Xử lý từng entry (word/kanji/grammar) ─────────────
    def _process_entry(self, cur, record: dict):
        meta = record.get("entry_metadata", {})
        dict_data = record.get("dictionary_data", {})
        entry_type = meta.get("entry_type", "word")

        if not meta.get("text"):
            log.warning("Bỏ qua record thiếu 'text': %s", record)
            self.stats["skipped"] += 1
            return

        # 1. Upsert dictionary_entry
        entry_id = self._upsert_entry(cur, meta)

        # 2. Insert examples
        examples = dict_data.get("examples", [])
        self._insert_examples(cur, entry_id, examples)

        # 3. Xử lý relations theo loại entry
        if entry_type == "word":
            for syn in dict_data.get("synonyms", []):
                self._insert_relation(cur, entry_id, syn, "synonym", "word")
            for ant in dict_data.get("antonyms", []):
                self._insert_relation(cur, entry_id, ant, "antonym", "word")
            for comp in dict_data.get("compounds", []):
                self._insert_relation(cur, entry_id, comp, "compound", "word")

            # kanji_list: lưu quan hệ word → kanji (relation_type = 'kanji')
            graph = record.get("graph_knowledge", {})
            for kanji_char in graph.get("kanji_list", []):
                if kanji_char:
                    self._insert_relation(cur, entry_id, kanji_char, "kanji", "kanji")

        elif entry_type == "kanji":
            # Kanji có thể chứa contains_words trong graph_knowledge
            graph = record.get("graph_knowledge", {})
            for word in graph.get("contains_words", []):
                self._insert_relation(cur, entry_id, word, "compound", "word")
            # radical components
            for comp in graph.get("components", []):
                if comp and comp != meta.get("text"):
                    self._insert_relation(cur, entry_id, comp, "radical", "kanji")

        elif entry_type == "grammar":
            for sim in dict_data.get("similar_grammar", []):
                self._insert_relation(cur, entry_id, sim, "synonym", "grammar")

    # ── Import một file JSON ───────────────────────────────
    def import_file(self, filepath: str):
        log.info("Đang xử lý file: %s", filepath)
        records = load_json_file(filepath)
        log.info("  → %d records", len(records))

        if self.dry_run:
            log.info("  [DRY RUN] Bỏ qua thực thi.")
            return

        with self.conn.cursor() as cur:
            for i, record in enumerate(records, start=1):
                try:
                    self._process_entry(cur, record)
                except Exception as e:
                    log.error("  Lỗi record #%d: %s — %s", i, record.get("entry_metadata", {}).get("text", "?"), e)
                    self.conn.rollback()
                    raise

        self.conn.commit()
        log.info("  ✓ Commit thành công.")

    # ── Import tất cả file trong thư mục ──────────────────
    def import_directory(self, data_dir: str):
        json_files = sorted(glob.glob(os.path.join(data_dir, "*.json")))
        if not json_files:
            log.warning("Không tìm thấy file JSON nào trong: %s", data_dir)
            return

        log.info("Tìm thấy %d file JSON trong '%s'", len(json_files), data_dir)
        for filepath in json_files:
            self.import_file(filepath)

        log.info(
            "\n📊 Kết quả import:\n"
            "   Entries mới  : %d\n"
            "   Examples mới : %d\n"
            "   Relations mới: %d\n"
            "   Bỏ qua       : %d",
            self.stats["entries"],
            self.stats["examples"],
            self.stats["relations"],
            self.stats["skipped"],
        )


# ══════════════════════════════════════════════════════════
#  ENTRY POINT
# ══════════════════════════════════════════════════════════

def parse_args():
    parser = argparse.ArgumentParser(
        description="Import dữ liệu JSON (word/kanji/grammar) vào PostgreSQL."
    )
    parser.add_argument(
        "--data-dir",
        default=str(Path(__file__).parent / "sample-data"),
        help="Đường dẫn thư mục chứa các file JSON (mặc định: ./sample-data)",
    )
    parser.add_argument(
        "--file",
        default=None,
        help="Import một file JSON cụ thể thay vì cả thư mục.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Chạy thử, không ghi vào DB.",
    )
    # Ghi đè config DB qua CLI (tùy chọn)
    parser.add_argument("--host", default=DB_CONFIG["host"])
    parser.add_argument("--port", type=int, default=DB_CONFIG["port"])
    parser.add_argument("--dbname", default=DB_CONFIG["dbname"])
    parser.add_argument("--user", default=DB_CONFIG["user"])
    parser.add_argument("--password", default=DB_CONFIG["password"])
    return parser.parse_args()


def main():
    args = parse_args()

    # Ghi đè DB_CONFIG nếu có tham số CLI
    db_cfg = {
        "host": args.host,
        "port": args.port,
        "dbname": args.dbname,
        "user": args.user,
        "password": args.password,
    }

    if args.dry_run:
        log.info("=== CHẾ ĐỘ DRY RUN – không ghi vào DB ===")
        conn = None
    else:
        log.info("Kết nối đến PostgreSQL: %s@%s:%s/%s", db_cfg["user"], db_cfg["host"], db_cfg["port"], db_cfg["dbname"])
        try:
            conn = psycopg2.connect(**db_cfg)
            conn.autocommit = False
        except psycopg2.OperationalError as e:
            log.error("Không thể kết nối DB: %s", e)
            sys.exit(1)

    importer = Importer(conn, dry_run=args.dry_run)

    try:
        if args.file:
            importer.import_file(args.file)
        else:
            importer.import_directory(args.data_dir)
    finally:
        if conn:
            conn.close()
            log.info("Đã đóng kết nối DB.")


if __name__ == "__main__":
    main()
