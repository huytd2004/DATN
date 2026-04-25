#!/usr/bin/env python3
"""
Script import dữ liệu từ các file JSON (word/kanji/grammar) vào Neo4j.

Cách dùng:
    python import_to_neo4j.py [--data-dir <thư mục>] [--dry-run]

Yêu cầu:
    pip install neo4j

Cấu hình kết nối Neo4j: chỉnh sửa NEO4J_CONFIG bên dưới.
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
    from neo4j import GraphDatabase
    from neo4j.exceptions import ServiceUnavailable
except ImportError:
    print("Lỗi: Chưa cài neo4j driver. Hãy chạy: pip install neo4j")
    sys.exit(1)

# ─────────────────────────────────────────────────────────
#  CẤU HÌNH KẾT NỐI NEO4J – chỉnh sửa tại đây
# ─────────────────────────────────────────────────────────
NEO4J_CONFIG = {
    "uri": "bolt://localhost:7687",
    "user": "neo4j",
    "password": "12345678",  # <-- đổi thành password Neo4j của bạn
    "database": "datn-graph",          # <-- mặc định là "neo4j"
}
# ─────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger(__name__)

# ══════════════════════════════════════════════════════════
#  MAPPING NHÃN NODE
# ══════════════════════════════════════════════════════════
ENTRY_TYPE_LABEL = {
    "word":    "Word",
    "kanji":   "Kanji",
    "grammar": "Grammar",
}

# relation_type từ graph_knowledge → tên relationship Neo4j
COLLOCATION_REL = {
    "DESCRIBED_BY":   "DESCRIBED_BY",
    "ACTION_OBJECT":  "ACTION_OBJECT",
    "CO_OCCUR":       "CO_OCCURS_WITH",
}


# ══════════════════════════════════════════════════════════
#  HELPERS
# ══════════════════════════════════════════════════════════

def load_json_file(filepath: str) -> list:
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


def normalize_jlpt(level: Optional[str]) -> Optional[str]:
    if level is None:
        return None
    level = str(level).strip().upper()
    return level if level in ("N5", "N4", "N3") else None


def gen_id() -> str:
    return str(uuid.uuid4())


# ══════════════════════════════════════════════════════════
#  IMPORTER
# ══════════════════════════════════════════════════════════

class Neo4jImporter:
    def __init__(self, driver, database: str = "neo4j", dry_run: bool = False):
        self.driver = driver
        self.database = database
        self.dry_run = dry_run
        self.stats = {
            "nodes": 0,
            "examples": 0,
            "relations": 0,
            "skipped": 0,
        }

    def _run(self, session, query: str, **params):
        """Chạy một Cypher query."""
        if self.dry_run:
            log.debug("[DRY RUN] %s | %s", query[:80], params)
            return None
        return session.run(query, **params)

    # ──────────────────────────────────────────────────────
    #  KHỞI TẠO CONSTRAINTS & INDEXES
    # ──────────────────────────────────────────────────────
    def setup_schema(self):
        """Tạo constraint unique cho mỗi loại node theo (text, jlpt_level)."""
        log.info("Thiết lập schema (constraints & indexes)...")
        constraints = [
            "CREATE CONSTRAINT word_text IF NOT EXISTS FOR (n:Word)    REQUIRE n.text IS UNIQUE",
            "CREATE CONSTRAINT kanji_text IF NOT EXISTS FOR (n:Kanji)  REQUIRE n.text IS UNIQUE",
            "CREATE CONSTRAINT grammar_text IF NOT EXISTS FOR (n:Grammar) REQUIRE n.text IS UNIQUE",
        ]
        if self.dry_run:
            log.info("[DRY RUN] Bỏ qua tạo schema.")
            return
        with self.driver.session(database=self.database) as session:
            for cql in constraints:
                try:
                    session.run(cql)
                    log.debug("  Schema OK: %s", cql[:60])
                except Exception as e:
                    log.warning("  Schema warning: %s", e)

    # ──────────────────────────────────────────────────────
    #  UPSERT NODE CHÍNH
    # ──────────────────────────────────────────────────────
    def _upsert_node(self, session, label: str, props: dict) -> str:
        """
        MERGE node theo text (unique). Trả về id đã lưu trong node.
        """
        entry_id = props.get("id") or gen_id()
        query = f"""
            MERGE (n:{label} {{text: $text}})
            ON CREATE SET
                n.id                = $id,
                n.reading           = $reading,
                n.meaning_vn        = $meaning_vn,
                n.jlpt_level        = $jlpt_level,
                n.explanation_short = $explanation_short,
                n.created_at        = timestamp()
            ON MATCH SET
                n.reading           = $reading,
                n.meaning_vn        = $meaning_vn,
                n.jlpt_level        = $jlpt_level,
                n.explanation_short = $explanation_short
            RETURN n.id AS nid
        """
        result = self._run(
            session, query,
            text=props["text"],
            id=entry_id,
            reading=props.get("reading"),
            meaning_vn=props.get("meaning_vn", ""),
            jlpt_level=props.get("jlpt_level"),
            explanation_short=props.get("explanation_short"),
        )
        if result:
            record = result.single()
            return record["nid"] if record else entry_id
        return entry_id

    # ──────────────────────────────────────────────────────
    #  UPSERT NODE PHỤ (stub, chưa biết đầy đủ thông tin)
    # ──────────────────────────────────────────────────────
    def _ensure_node(self, session, label: str, text: str) -> str:
        """Tạo node nếu chưa tồn tại (stub). Trả về id."""
        stub_id = gen_id()
        query = f"""
            MERGE (n:{label} {{text: $text}})
            ON CREATE SET n.id = $id, n.created_at = timestamp()
            RETURN n.id AS nid
        """
        result = self._run(session, query, text=text, id=stub_id)
        if result:
            record = result.single()
            return record["nid"] if record else stub_id
        return stub_id

    # ──────────────────────────────────────────────────────
    #  TẠO RELATIONSHIP
    # ──────────────────────────────────────────────────────
    def _merge_rel(self, session, src_label: str, src_text: str,
                   rel_type: str, tgt_label: str, tgt_text: str,
                   rel_props: Optional[dict] = None):
        """MERGE một relationship giữa hai node theo text."""
        props_str = ""
        if rel_props:
            props_str = " {" + ", ".join(f"{k}: ${k}" for k in rel_props) + "}"

        query = f"""
            MATCH (a:{src_label} {{text: $src_text}})
            MERGE (b:{tgt_label} {{text: $tgt_text}})
                ON CREATE SET b.id = $new_id, b.created_at = timestamp()
            MERGE (a)-[r:{rel_type}{props_str}]->(b)
        """
        params = {"src_text": src_text, "tgt_text": tgt_text, "new_id": gen_id()}
        if rel_props:
            params.update(rel_props)
        self._run(session, query, **params)
        self.stats["relations"] += 1

    # ──────────────────────────────────────────────────────
    #  INSERT EXAMPLE NODES
    # ──────────────────────────────────────────────────────
    def _insert_examples(self, session, parent_label: str, parent_text: str, examples: list):
        """Tạo node Example và relationship HAS_EXAMPLE."""
        for ex in examples:
            jp = ex.get("japanese_sentence", "")
            vn = ex.get("vietnamese_sentence", "")
            if not jp:
                continue
            query = f"""
                MATCH (p:{parent_label} {{text: $parent_text}})
                MERGE (e:Example {{japanese_sentence: $jp}})
                    ON CREATE SET e.id = $eid, e.vietnamese_sentence = $vn
                MERGE (p)-[:HAS_EXAMPLE]->(e)
            """
            self._run(session, query,
                      parent_text=parent_text,
                      jp=jp, vn=vn,
                      eid=gen_id())
            self.stats["examples"] += 1

    # ──────────────────────────────────────────────────────
    #  XỬ LÝ WORD ENTRY
    # ──────────────────────────────────────────────────────
    def _process_word(self, session, record: dict):
        meta = record["entry_metadata"]
        dict_data = record.get("dictionary_data", {})
        graph = record.get("graph_knowledge", {})
        text = meta["text"]
        label = "Word"

        # 1. Upsert node Word
        self._upsert_node(session, label, {
            "text": text,
            "reading": meta.get("reading"),
            "meaning_vn": meta.get("meaning_vn", ""),
            "jlpt_level": normalize_jlpt(meta.get("jlpt_level")),
            "explanation_short": meta.get("explanation_short"),
        })
        self.stats["nodes"] += 1

        # 2. Examples
        self._insert_examples(session, label, text, dict_data.get("examples", []))

        # 3. Synonyms / Antonyms / Compounds → Word
        for syn in dict_data.get("synonyms", []):
            self._merge_rel(session, label, text, "SYNONYM", "Word", syn)
        for ant in dict_data.get("antonyms", []):
            self._merge_rel(session, label, text, "ANTONYM", "Word", ant)
        for comp in dict_data.get("compounds", []):
            self._merge_rel(session, label, text, "COMPOUND", "Word", comp)

        # 4. kanji_list → Kanji (CONTAINS_KANJI)
        for kanji_char in graph.get("kanji_list", []):
            if kanji_char:
                self._merge_rel(session, label, text, "CONTAINS_KANJI", "Kanji", kanji_char)

        # 5. Common collocations (DESCRIBED_BY, ACTION_OBJECT, CO_OCCURS_WITH)
        for col in graph.get("common_collocations", []):
            col_text = col.get("text", "")
            col_rel  = col.get("relation", "CO_OCCURS_WITH")
            neo4j_rel = COLLOCATION_REL.get(col_rel, col_rel)
            if col_text:
                self._merge_rel(session, label, text, neo4j_rel, "Word", col_text)

        # 6. Confusable with
        for conf in graph.get("confusable_with", []):
            conf_text   = conf.get("text", "")
            conf_reason = conf.get("reason", "")
            if conf_text:
                self._merge_rel(session, label, text, "CONFUSABLE_WITH", "Word", conf_text,
                                rel_props={"reason": conf_reason})

    # ──────────────────────────────────────────────────────
    #  XỬ LÝ KANJI ENTRY
    # ──────────────────────────────────────────────────────
    def _process_kanji(self, session, record: dict):
        meta      = record["entry_metadata"]
        dict_data = record.get("dictionary_data", {})
        graph     = record.get("graph_knowledge", {})
        text      = meta["text"]
        label     = "Kanji"

        # 1. Upsert node Kanji (kèm thông tin đặc thù)
        stub_id = gen_id()
        query = """
            MERGE (n:Kanji {text: $text})
            ON CREATE SET
                n.id                = $id,
                n.reading           = $reading,
                n.meaning_vn        = $meaning_vn,
                n.jlpt_level        = $jlpt_level,
                n.explanation_short = $explanation_short,
                n.on_yomi           = $on_yomi,
                n.kun_yomi          = $kun_yomi,
                n.stroke_count      = $stroke_count,
                n.mnemonic          = $mnemonic,
                n.created_at        = timestamp()
            ON MATCH SET
                n.reading           = $reading,
                n.meaning_vn        = $meaning_vn,
                n.jlpt_level        = $jlpt_level,
                n.explanation_short = $explanation_short,
                n.on_yomi           = $on_yomi,
                n.kun_yomi          = $kun_yomi,
                n.stroke_count      = $stroke_count,
                n.mnemonic          = $mnemonic
            RETURN n.id AS nid
        """
        self._run(
            session, query,
            text=text,
            id=stub_id,
            reading=meta.get("reading"),
            meaning_vn=meta.get("meaning_vn", ""),
            jlpt_level=normalize_jlpt(meta.get("jlpt_level")),
            explanation_short=meta.get("explanation_short"),
            on_yomi=dict_data.get("on_yomi", []),
            kun_yomi=dict_data.get("kun_yomi", []),
            stroke_count=dict_data.get("stroke_count"),
            mnemonic=graph.get("mnemonic"),
        )
        self.stats["nodes"] += 1

        # 2. Examples
        self._insert_examples(session, label, text, dict_data.get("examples", []))

        # 3. contains_words → Word (APPEARS_IN)
        for word in graph.get("contains_words", []):
            self._merge_rel(session, label, text, "APPEARS_IN", "Word", word)

        # 4. Components → Kanji (HAS_COMPONENT)
        for comp in graph.get("components", []):
            if comp and comp != text:
                self._merge_rel(session, label, text, "HAS_COMPONENT", "Kanji", comp)

        # 5. Radicals → Kanji (HAS_RADICAL)
        for rad in graph.get("radicals", []):
            if rad and rad != text:
                self._merge_rel(session, label, text, "HAS_RADICAL", "Kanji", rad)

        # 6. Visual similarity → Kanji (VISUALLY_SIMILAR)
        for vs in graph.get("visual_similarity", []):
            vs_text   = vs.get("text", "")
            vs_reason = vs.get("reason", "")
            if vs_text:
                self._merge_rel(session, label, text, "VISUALLY_SIMILAR", "Kanji", vs_text,
                                rel_props={"reason": vs_reason})

    # ──────────────────────────────────────────────────────
    #  XỬ LÝ GRAMMAR ENTRY
    # ──────────────────────────────────────────────────────
    def _process_grammar(self, session, record: dict):
        meta      = record["entry_metadata"]
        dict_data = record.get("dictionary_data", {})
        graph     = record.get("graph_knowledge", {})
        text      = meta["text"]
        label     = "Grammar"

        # 1. Upsert node Grammar (kèm thông tin đặc thù)
        stub_id = gen_id()
        query = """
            MERGE (n:Grammar {text: $text})
            ON CREATE SET
                n.id                  = $id,
                n.meaning_vn          = $meaning_vn,
                n.jlpt_level          = $jlpt_level,
                n.explanation_short   = $explanation_short,
                n.formation           = $formation,
                n.grammatical_category= $grammatical_category,
                n.politeness_level    = $politeness_level,
                n.created_at          = timestamp()
            ON MATCH SET
                n.meaning_vn          = $meaning_vn,
                n.jlpt_level          = $jlpt_level,
                n.explanation_short   = $explanation_short,
                n.formation           = $formation,
                n.grammatical_category= $grammatical_category,
                n.politeness_level    = $politeness_level
            RETURN n.id AS nid
        """
        self._run(
            session, query,
            text=text,
            id=stub_id,
            meaning_vn=meta.get("meaning_vn", ""),
            jlpt_level=normalize_jlpt(meta.get("jlpt_level")),
            explanation_short=meta.get("explanation_short"),
            formation=dict_data.get("formation", []),
            grammatical_category=graph.get("grammatical_category"),
            politeness_level=graph.get("politeness_level"),
        )
        self.stats["nodes"] += 1

        # 2. Examples
        self._insert_examples(session, label, text, dict_data.get("examples", []))

        # 3. Similar grammar (SIMILAR_TO)
        for sim in dict_data.get("similar_grammar", []):
            if sim:
                self._merge_rel(session, label, text, "SIMILAR_TO", "Grammar", sim)

        # 4. Confusable with
        for conf in graph.get("confusable_with", []):
            conf_text   = conf.get("text", "")
            conf_reason = conf.get("reason", "")
            if conf_text:
                self._merge_rel(session, label, text, "CONFUSABLE_WITH", "Grammar", conf_text,
                                rel_props={"reason": conf_reason})

    # ──────────────────────────────────────────────────────
    #  DISPATCH
    # ──────────────────────────────────────────────────────
    def _process_record(self, session, record: dict):
        meta = record.get("entry_metadata", {})
        entry_type = meta.get("entry_type", "")
        text = meta.get("text", "")

        if not text:
            log.warning("Bỏ qua record thiếu 'text': %s", record)
            self.stats["skipped"] += 1
            return

        if entry_type == "word":
            self._process_word(session, record)
        elif entry_type == "kanji":
            self._process_kanji(session, record)
        elif entry_type == "grammar":
            self._process_grammar(session, record)
        else:
            log.warning("entry_type không rõ: '%s' – bỏ qua.", entry_type)
            self.stats["skipped"] += 1

    # ──────────────────────────────────────────────────────
    #  IMPORT FILE
    # ──────────────────────────────────────────────────────
    def import_file(self, filepath: str):
        log.info("Đang xử lý file: %s", filepath)
        records = load_json_file(filepath)
        log.info("  → %d records", len(records))

        if self.dry_run:
            log.info("  [DRY RUN] Bỏ qua thực thi.")
            return

        with self.driver.session(database=self.database) as session:
            for i, record in enumerate(records, start=1):
                try:
                    self._process_record(session, record)
                except Exception as e:
                    text = record.get("entry_metadata", {}).get("text", "?")
                    log.error("  Lỗi record #%d ('%s'): %s", i, text, e)
                    raise

        log.info("  ✓ Xong file.")

    # ──────────────────────────────────────────────────────
    #  IMPORT THƯ MỤC
    # ──────────────────────────────────────────────────────
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
            "   Nodes mới / cập nhật : %d\n"
            "   Example nodes        : %d\n"
            "   Relationships        : %d\n"
            "   Bỏ qua               : %d",
            self.stats["nodes"],
            self.stats["examples"],
            self.stats["relations"],
            self.stats["skipped"],
        )


# ══════════════════════════════════════════════════════════
#  ENTRY POINT
# ══════════════════════════════════════════════════════════

def parse_args():
    parser = argparse.ArgumentParser(
        description="Import dữ liệu JSON (word/kanji/grammar) vào Neo4j."
    )
    parser.add_argument(
        "--data-dir",
        default=str(Path(__file__).parent / "sample-data"),
        help="Thư mục chứa JSON (mặc định: ./sample-data)",
    )
    parser.add_argument("--file", default=None, help="Import một file JSON cụ thể.")
    parser.add_argument("--dry-run", action="store_true", help="Chạy thử, không ghi vào Neo4j.")
    parser.add_argument("--uri",      default=NEO4J_CONFIG["uri"])
    parser.add_argument("--user",     default=NEO4J_CONFIG["user"])
    parser.add_argument("--password", default=NEO4J_CONFIG["password"])
    parser.add_argument("--database", default=NEO4J_CONFIG["database"])
    return parser.parse_args()


def main():
    args = parse_args()

    if args.dry_run:
        log.info("=== CHẾ ĐỘ DRY RUN – không ghi vào Neo4j ===")
        driver = None
    else:
        log.info("Kết nối Neo4j: %s (user=%s, db=%s)", args.uri, args.user, args.database)
        try:
            driver = GraphDatabase.driver(args.uri, auth=(args.user, args.password))
            driver.verify_connectivity()
            log.info("Kết nối thành công.")
        except ServiceUnavailable as e:
            log.error("Không thể kết nối Neo4j: %s", e)
            sys.exit(1)

    importer = Neo4jImporter(driver, database=args.database, dry_run=args.dry_run)

    try:
        importer.setup_schema()
        if args.file:
            importer.import_file(args.file)
        else:
            importer.import_directory(args.data_dir)
    finally:
        if driver:
            driver.close()
            log.info("Đã đóng kết nối Neo4j.")


if __name__ == "__main__":
    main()
