"""
Pipeline stateless cho dịch đoạn văn chuyên ngành Nhật → Việt.

Nguyên tắc (theo schema-neo4j.md):
- Neo4j chỉ query read-only theo token surface.
- Không tạo Sentence/Token node trong Neo4j.
- Tokenization, domain detection, sense ranking hoàn toàn in-memory.
- Lịch sử bản dịch (nếu cần) lưu PostgreSQL ở backend.
"""

import json
import re

from .neo4j_client import Neo4jClient
from .ranker import Ranker
from .prompt_builder import build_prompt
from .llm_client import LLMClient

# ── Domain keyword map (in-memory detection) ──────────────────────────────────
DOMAIN_KEYWORDS: dict[str, list[str]] = {
    'technology':    ['半導体', 'プロセス', 'チップ', '回路', 'デバイス', 'ソフトウェア', '電子'],
    'semiconductor': ['半導体', '欠陥', 'ウエハ', 'リソグラフィ', 'トランジスタ'],
    'medicine':      ['病気', '症状', '治療', '手術', '診断', '薬', '患者', '医療'],
    'culture':       ['礼儀', '配慮', '伝統', '作法', '祭り', '敬語', '文化'],
    'academic':      ['研究', '論文', '実験', '理論', '仮説', '分析', '考察'],
}

# ── Stopword POS tags (MeCab) ─────────────────────────────────────────────────
STOP_POS = {'助詞', '助動詞', '記号', '補助記号', '空白'}


def _tokenize_in_memory(text: str) -> list[dict]:
    """
    Tokenize văn bản Nhật bằng MeCab. Trả về list token dict.
    Lọc bỏ stop-words (particle, punctuation).
    KHÔNG tạo bất kỳ node Neo4j nào.
    """
    try:
        import MeCab
        tagger = MeCab.Tagger()
        tokens = []
        position = 0
        node = tagger.parseToNode(text)
        while node:
            surface = node.surface
            features = node.feature.split(',')
            pos = features[0] if features else ''
            if surface and pos not in STOP_POS:
                tokens.append({
                    'surface':  surface,
                    'reading':  features[7] if len(features) > 7 else surface,
                    'pos':      pos,
                    'position': position,
                })
                position += 1
            node = node.next
        return tokens
    except ImportError:
        # Fallback: split đơn giản nếu MeCab chưa cài
        return [
            {'surface': w, 'reading': w, 'pos': 'unknown', 'position': i}
            for i, w in enumerate(text.split())
            if w.strip()
        ]


def _detect_domains_in_memory(surfaces: list[str]) -> list[str]:
    """
    Phát hiện domain từ keyword matching in-memory.
    Trả về list domain, mặc định ['general'] nếu không detect được.
    """
    detected = set()
    surface_set = set(surfaces)
    for domain, keywords in DOMAIN_KEYWORDS.items():
        if any(kw in surface_set for kw in keywords):
            detected.add(domain)
    return list(detected) if detected else ['general']


def _parse_llm_json(raw_text: str) -> tuple[str, list[dict]]:
    """
    Parse JSON output từ LLM. Xử lý trường hợp LLM bọc trong markdown code block.
    Returns (translation, notes).
    """
    text = raw_text.strip()
    # Strip markdown code block nếu có
    text = re.sub(r'^```(?:json)?\s*', '', text, flags=re.MULTILINE)
    text = re.sub(r'```\s*$', '', text, flags=re.MULTILINE)
    text = text.strip()
    try:
        parsed = json.loads(text)
        return parsed.get('translation', ''), parsed.get('notes', [])
    except json.JSONDecodeError:
        # Fallback: treat toàn bộ text là bản dịch
        return text, []


def _post_check(translation: str, ranked_senses: list[dict], notes: list[dict]) -> list[dict]:
    """
    Kiểm tra consistency: glossVi của key term có xuất hiện trong bản dịch không.
    Thêm warning vào notes nếu cần.
    """
    for rs in ranked_senses:
        top = rs['top_sense']
        gloss_vi = top.get('glossVi', '')
        if gloss_vi and gloss_vi not in translation:
            notes.append({
                'type':    'consistency_warning',
                'token':   rs['surface'],
                'content': f"'{rs['surface']}' → glossVi '{gloss_vi}' không xuất hiện rõ trong bản dịch.",
            })
    return notes


class Pipeline:
    def __init__(self, neo4j_client=None, ranker=None, llm=None):
        self.neo = neo4j_client or Neo4jClient()
        self.ranker = ranker or Ranker()
        self.llm = llm or LLMClient()

    def translate(self, input_text: str) -> dict:
        """
        Dịch đoạn văn chuyên ngành Nhật → Việt (stateless).

        Returns:
            {
              "translation":   str,
              "keyVocabulary": list[dict],
              "notes":         list[dict],
              "warnings":      list[dict],
              "detectedDomains": list[str],
            }
        """
        # ── Bước 1: Tokenize in-memory ────────────────────────────────────────
        tokens = _tokenize_in_memory(input_text)
        surfaces = [t['surface'] for t in tokens]

        # ── Bước 2: Detect domain in-memory ──────────────────────────────────
        detected_domains = _detect_domains_in_memory(surfaces)

        # ── Bước 3: Batch query Neo4j (read-only, stateless) ─────────────────
        unique_surfaces = list(dict.fromkeys(surfaces))  # preserve order, deduplicate
        graph_evidence = self.neo.batch_query_by_surfaces(unique_surfaces, detected_domains)

        # ── Bước 4: Rank senses in-memory ────────────────────────────────────
        ranked_senses, key_vocabulary = self.ranker.rank(graph_evidence, detected_domains)

        # ── Bước 5: Build prompt ──────────────────────────────────────────────
        prompt = build_prompt(input_text, ranked_senses, detected_domains)

        # ── Bước 6: LLM translate ─────────────────────────────────────────────
        resp = self.llm.complete(prompt, max_tokens=1024)
        translation, notes = _parse_llm_json(resp.get('text', ''))

        # ── Bước 7: Post-check consistency ───────────────────────────────────
        notes = _post_check(translation, ranked_senses, notes)

        # ── Bước 8: Format output ─────────────────────────────────────────────
        warnings = [n for n in notes if n.get('type') == 'consistency_warning']
        clean_notes = [n for n in notes if n.get('type') != 'consistency_warning']

        return {
            'translation':     translation,
            'keyVocabulary':   key_vocabulary,
            'notes':           clean_notes,
            'warnings':        warnings,
            'detectedDomains': detected_domains,
        }


# ── CLI ───────────────────────────────────────────────────────────────────────

def main_cli():
    import argparse
    parser = argparse.ArgumentParser(description='Stateless translation pipeline')
    parser.add_argument('--text', required=True, help='Đoạn văn tiếng Nhật cần dịch')
    parser.add_argument('--json', action='store_true', help='Xuất JSON thay vì plain text')
    args = parser.parse_args()

    p = Pipeline()
    out = p.translate(args.text)

    if args.json:
        print(json.dumps(out, ensure_ascii=False, indent=2))
    else:
        print(out['translation'])
        if out.get('keyVocabulary'):
            print('\n--- Từ vựng quan trọng ---')
            for kv in out['keyVocabulary']:
                print(f"  {kv['surface']} ({kv.get('reading', '')}) [{kv.get('jlpt', '?')}] → {kv.get('glossVi', '')}")
        if out.get('notes'):
            print('\n--- Ghi chú ---')
            for n in out['notes']:
                print(f"  [{n.get('type')}] {n.get('token')}: {n.get('content')}")


if __name__ == '__main__':
    main_cli()
