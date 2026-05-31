# LangGraph Pipeline cho Dịch Đoạn Văn Chuyên Ngành

## Tổng quan

Pipeline này implement hệ thống dịch Nhật → Việt chuyên ngành sử dụng LangGraph, với nguyên tắc:
- **Neo4j**: read-only, chỉ query knowledge graph tĩnh.
- **Tokenization / sense ranking**: hoàn toàn in-memory.
- **Không tạo bất kỳ Sentence hay Token node** nào trong Neo4j.

---

## 1. Kiến trúc Graph (LangGraph)

```
[START]
   ↓
[segment_sentences]        # Tách câu in-memory
   ↓
[tokenize_and_detect]      # Tokenize + detect domain in-memory
   ↓
[batch_neo4j_query]        # Batch query Neo4j theo surface list
   ↓
[rank_senses]              # Score và chọn sense tốt nhất in-memory
   ↓
[build_prompt]             # Tạo structured prompt
   ↓
[llm_translate]            # LLM sinh bản dịch + notes
   ↓
[post_check]               # Kiểm tra consistency thuật ngữ
   ↓
[format_output]            # Tổng hợp output cuối
   ↓
[END]
```

---

## 2. State Schema

```python
from typing import TypedDict, Optional

class TranslationState(TypedDict):
    # === INPUT ===
    input_text: str                    # Đoạn văn gốc (tiếng Nhật)

    # === IN-MEMORY RUNTIME ===
    sentences: list[str]               # Câu sau khi tách
    tokens_per_sentence: list[list[dict]]  # [{surface, reading, pos, position}]
    detected_domains: list[str]        # Domain phát hiện (in-memory)
    context_surfaces: list[str]        # Tất cả content token surfaces (flat)

    # === NEO4J EVIDENCE (read-only result) ===
    graph_evidence: list[dict]         # Kết quả batch query Neo4j

    # === RANKED SENSES (in-memory) ===
    ranked_senses: list[dict]          # {surface, top_sense, score, alternatives}
    key_vocabulary: list[dict]         # Các từ vựng quan trọng cần highlight

    # === LLM OUTPUT ===
    prompt: str                        # Prompt đã build
    translation: str                   # Bản dịch cuối
    notes: list[dict]                  # [{type, token, content}]

    # === META ===
    error: Optional[str]
```

---

## 3. Chi tiết từng Node

### 3.1 `segment_sentences`

```python
def segment_sentences(state: TranslationState) -> TranslationState:
    """
    Tách đoạn văn thành list câu.
    Dùng regex hoặc thư viện NLP (e.g. ja_sentence_segmenter).
    KHÔNG tạo node Neo4j.
    """
    import re
    text = state["input_text"]
    # Tách theo dấu câu Nhật: 。！？
    sentences = re.split(r'(?<=[。！？])', text)
    sentences = [s.strip() for s in sentences if s.strip()]
    return {**state, "sentences": sentences}
```

### 3.2 `tokenize_and_detect`

```python
def tokenize_and_detect(state: TranslationState) -> TranslationState:
    """
    Tokenize từng câu bằng MeCab.
    Detect domain từ keyword matching in-memory.
    KHÔNG tạo Token/Sentence node Neo4j.
    """
    import MeCab

    DOMAIN_KEYWORDS = {
        "technology":    ["半導体", "プロセス", "チップ", "回路", "デバイス"],
        "medicine":      ["病気", "症状", "治療", "手術", "診断"],
        "culture":       ["礼儀", "配慮", "伝統", "作法", "祭り"],
        "semiconductor": ["半導体", "欠陥", "ウエハ", "リソグラフィ"],
    }

    tagger = MeCab.Tagger()
    tokens_per_sentence = []
    all_surfaces = []
    detected_domains = set()

    STOP_POS = {"助詞", "助動詞", "記号", "補助記号"}

    for sentence in state["sentences"]:
        tokens = []
        node = tagger.parseToNode(sentence)
        position = 0
        while node:
            surface = node.surface
            features = node.feature.split(",")
            pos = features[0]

            if surface and pos not in STOP_POS:
                token = {
                    "surface": surface,
                    "reading": features[7] if len(features) > 7 else surface,
                    "pos": pos,
                    "position": position,
                }
                tokens.append(token)
                all_surfaces.append(surface)
                position += 1

                # Detect domain
                for domain, keywords in DOMAIN_KEYWORDS.items():
                    if surface in keywords:
                        detected_domains.add(domain)

            node = node.next

        tokens_per_sentence.append(tokens)

    return {
        **state,
        "tokens_per_sentence": tokens_per_sentence,
        "context_surfaces": list(set(all_surfaces)),
        "detected_domains": list(detected_domains) or ["general"],
    }
```

### 3.3 `batch_neo4j_query`

```python
def batch_neo4j_query(state: TranslationState) -> TranslationState:
    """
    Batch query Neo4j một lần cho toàn bộ token surfaces.
    Read-only. Không tạo bất kỳ node nào.
    """
    from neo4j import GraphDatabase

    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))

    # Query 5.4 từ schema-neo4j.md (Batch Retrieval)
    CYPHER = """
    UNWIND $tokenSurfaces AS surface
    MATCH (lex:Lexeme {surface: surface})
      -[:HAS_SENSE]->(sense:Sense)
    OPTIONAL MATCH (sense)-[:BELONGS_TO]->(dom:Domain)
    OPTIONAL MATCH (sense)-[:HAS_REGISTER]->(reg:Register)
    OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(cue:Cue)
    OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
    OPTIONAL MATCH (sense)-[:REFERS_TO]->(ent:Entity)
    OPTIONAL MATCH (sense)-[:HAS_NOTE]->(note:CulturalNote)
    WITH surface, lex, sense, dom, reg, ent, note,
         collect(DISTINCT cue.surface) AS cues,
         collect(DISTINCT {ja: ex.ja, vi: ex.vi, quality: ex.qualityScore}) AS examples
    RETURN
      surface AS token,
      lex.reading AS reading,
      lex.jlpt AS jlpt,
      lex.pos AS pos,
      sense.senseId AS senseId,
      sense.glossVi AS glossVi,
      sense.glossEn AS glossEn,
      sense.definition AS definition,
      sense.usageNote AS usageNote,
      sense.confidenceBase AS baseConfidence,
      dom.name AS domain,
      reg.name AS register,
      ent.name AS entity,
      cues,
      examples,
      note.content AS culturalNote
    ORDER BY token
    """

    with driver.session() as session:
        result = session.run(
            CYPHER,
            tokenSurfaces=state["context_surfaces"]
        )
        graph_evidence = [dict(record) for record in result]

    driver.close()
    return {**state, "graph_evidence": graph_evidence}
```

### 3.4 `rank_senses`

```python
def rank_senses(state: TranslationState) -> TranslationState:
    """
    Rank sense theo scoring formula từ schema-neo4j.md Section 11.
    Chọn top sense cho mỗi token. Xác định key vocabulary.
    Hoàn toàn in-memory.
    """
    # Trọng số từ schema Section 11
    W = {
        "domain": 0.30,
        "register": 0.15,
        "cue": 0.25,
        "collocation": 0.15,
        "example": 0.10,
        "base": 0.05,
    }

    detected_domains = set(state["detected_domains"])
    neighbor_surfaces = set(state["context_surfaces"])

    # Group evidence by token surface
    evidence_by_token: dict[str, list[dict]] = {}
    for row in state["graph_evidence"]:
        token = row["token"]
        evidence_by_token.setdefault(token, []).append(row)

    ranked_senses = []
    key_vocabulary = []

    for token, senses in evidence_by_token.items():
        scored = []
        for sense in senses:
            score = 0.0
            # Domain match
            if sense.get("domain") in detected_domains:
                score += W["domain"]
            # Register match (kỹ thuật ưu tiên nếu domain kỹ thuật)
            if sense.get("register") in ("technical", "formal"):
                score += W["register"] * 0.8
            # Cue match: cue có trong neighbor surfaces không
            cues = sense.get("cues") or []
            if any(c in neighbor_surfaces for c in cues):
                score += W["cue"]
            # Example quality
            examples = sense.get("examples") or []
            if examples:
                avg_quality = sum(e.get("quality", 0) for e in examples) / len(examples)
                score += W["example"] * avg_quality
            # Base confidence
            score += W["base"] * (sense.get("baseConfidence") or 0)

            scored.append({**sense, "_score": score})

        # Sort và lấy top sense
        scored.sort(key=lambda x: x["_score"], reverse=True)
        top = scored[0]

        ranked_senses.append({
            "surface": token,
            "top_sense": top,
            "score": top["_score"],
            "alternatives": scored[1:3],  # Giữ 2 alternative
        })

        # Key vocabulary: JLPT ≤ 3 hoặc domain-specific hoặc polysemy
        jlpt = top.get("jlpt")
        is_polysemy = len(scored) >= 2
        is_domain_specific = top.get("domain") in detected_domains
        if jlpt and jlpt <= 3 or is_domain_specific or is_polysemy:
            key_vocabulary.append({
                "surface": token,
                "reading": top.get("reading", ""),
                "jlpt": jlpt,
                "glossVi": top.get("glossVi", ""),
                "domain": top.get("domain"),
                "register": top.get("register"),
            })

    return {**state, "ranked_senses": ranked_senses, "key_vocabulary": key_vocabulary}
```

### 3.5 `build_prompt`

```python
def build_prompt(state: TranslationState) -> TranslationState:
    """
    Tạo structured prompt cho LLM từ evidence đã rank.
    """
    source_text = state["input_text"]
    domains = ", ".join(state["detected_domains"])

    evidence_lines = []
    for rs in state["ranked_senses"]:
        top = rs["top_sense"]
        line = (
            f"- Token: {rs['surface']} "
            f"→ Sense: {top.get('glossVi', '?')} "
            f"| Domain: {top.get('domain', '?')} "
            f"| Register: {top.get('register', '?')}"
        )
        if top.get("culturalNote"):
            line += f"\n  ⚠ Cultural note: {top['culturalNote']}"
        if top.get("usageNote"):
            line += f"\n  📌 Usage: {top['usageNote']}"
        if top.get("examples"):
            ex = top["examples"][0]
            line += f"\n  Ví dụ: {ex.get('ja', '')} → {ex.get('vi', '')}"
        evidence_lines.append(line)

    evidence_block = "\n".join(evidence_lines)

    prompt = f"""Bạn là dịch giả chuyên ngành Nhật-Việt.
Nhiệm vụ: Dịch đoạn văn tiếng Nhật sang tiếng Việt.

Yêu cầu:
- Giữ đúng domain ({domains}) và register của từng thuật ngữ.
- Ưu tiên nghĩa theo evidence từ knowledge graph bên dưới.
- Nếu từ đa nghĩa, dùng sense có score cao nhất.
- Giữ văn phong tự nhiên, không dịch literal từng từ.
- Nếu có sắc thái văn hóa, diễn đạt tự nhiên theo tiếng Việt.

Source text:
{source_text}

Graph evidence:
{evidence_block}

Output (JSON):
{{
  "translation": "<bản dịch tự nhiên>",
  "notes": [
    {{"type": "polysemy|cultural|register|technical", "token": "<từ>", "content": "<giải thích ngắn>"}}
  ]
}}
"""
    return {**state, "prompt": prompt}
```

### 3.6 `llm_translate`

```python
def llm_translate(state: TranslationState) -> TranslationState:
    """
    Gọi LLM với structured prompt.
    Parse JSON output.
    """
    import json
    from langchain_openai import ChatOpenAI  # hoặc Gemini, Anthropic

    llm = ChatOpenAI(model="gpt-4o", temperature=0.2)
    response = llm.invoke(state["prompt"])

    try:
        content = response.content.strip()
        # Xử lý nếu LLM bọc trong ```json ... ```
        if content.startswith("```"):
            content = content.split("```")[1]
            if content.startswith("json"):
                content = content[4:]
        result = json.loads(content)
        translation = result.get("translation", "")
        notes = result.get("notes", [])
    except Exception:
        translation = response.content
        notes = []

    return {**state, "translation": translation, "notes": notes}
```

### 3.7 `post_check`

```python
def post_check(state: TranslationState) -> TranslationState:
    """
    Kiểm tra nhất quán thuật ngữ trong bản dịch.
    Rule-based: đảm bảo glossVi của key term xuất hiện đúng.
    """
    translation = state["translation"]
    notes = state["notes"]

    for rs in state["ranked_senses"]:
        top = rs["top_sense"]
        gloss_vi = top.get("glossVi", "")
        surface = rs["surface"]

        # Nếu gloss_vi không xuất hiện trong bản dịch (có thể LLM dùng từ khác)
        # → Thêm note cảnh báo để review thủ công nếu cần
        if gloss_vi and gloss_vi not in translation:
            notes.append({
                "type": "consistency_warning",
                "token": surface,
                "content": f"'{surface}' → glossVi '{gloss_vi}' không xuất hiện rõ trong bản dịch. Kiểm tra lại."
            })

    return {**state, "notes": notes}
```

### 3.8 `format_output`

```python
def format_output(state: TranslationState) -> TranslationState:
    """
    Tổng hợp output cuối theo format schema-neo4j.md Section 11.5.
    Không lưu vào Neo4j. Backend lưu vào PostgreSQL nếu cần lịch sử.
    """
    output = {
        "translation": state["translation"],
        "keyVocabulary": state["key_vocabulary"],
        "notes": [n for n in state["notes"] if n.get("type") != "consistency_warning"],
        "warnings": [n for n in state["notes"] if n.get("type") == "consistency_warning"],
        "detectedDomains": state["detected_domains"],
    }
    # Attach vào state để caller lấy
    return {**state, "_output": output}
```

---

## 4. Khởi tạo Graph

```python
from langgraph.graph import StateGraph, END

def build_translation_graph() -> StateGraph:
    graph = StateGraph(TranslationState)

    graph.add_node("segment_sentences",   segment_sentences)
    graph.add_node("tokenize_and_detect", tokenize_and_detect)
    graph.add_node("batch_neo4j_query",   batch_neo4j_query)
    graph.add_node("rank_senses",         rank_senses)
    graph.add_node("build_prompt",        build_prompt)
    graph.add_node("llm_translate",       llm_translate)
    graph.add_node("post_check",          post_check)
    graph.add_node("format_output",       format_output)

    graph.set_entry_point("segment_sentences")
    graph.add_edge("segment_sentences",   "tokenize_and_detect")
    graph.add_edge("tokenize_and_detect", "batch_neo4j_query")
    graph.add_edge("batch_neo4j_query",   "rank_senses")
    graph.add_edge("rank_senses",         "build_prompt")
    graph.add_edge("build_prompt",        "llm_translate")
    graph.add_edge("llm_translate",       "post_check")
    graph.add_edge("post_check",          "format_output")
    graph.add_edge("format_output",       END)

    return graph.compile()
```

---

## 5. Sử dụng

```python
app = build_translation_graph()

result = app.invoke({
    "input_text": "半導体の製造プロセスでは、微細な欠陥が性能に大きく影響する。",
    "sentences": [],
    "tokens_per_sentence": [],
    "detected_domains": [],
    "context_surfaces": [],
    "graph_evidence": [],
    "ranked_senses": [],
    "key_vocabulary": [],
    "prompt": "",
    "translation": "",
    "notes": [],
    "error": None,
})

output = result["_output"]
print(output["translation"])
# → "Trong quy trình sản xuất bán dẫn, các khuyết tật vi mô có ảnh hưởng lớn đến hiệu năng."

print(output["keyVocabulary"])
# → [{"surface": "半導体", "reading": "はんどうたい", "jlpt": 2, "glossVi": "bán dẫn", ...}]
```

---

## 6. Nguyên tắc thiết kế

| Node | Neo4j? | Ghi chú |
|---|---|---|
| `segment_sentences` | ❌ | Regex / NLP library, in-memory |
| `tokenize_and_detect` | ❌ | MeCab, in-memory hoàn toàn |
| `batch_neo4j_query` | ✅ Read-only | 1 query duy nhất với `UNWIND` |
| `rank_senses` | ❌ | Scoring formula in-memory |
| `build_prompt` | ❌ | String template, in-memory |
| `llm_translate` | ❌ | LLM API call |
| `post_check` | ❌ | Rule-based, in-memory |
| `format_output` | ❌ | Tổng hợp output |

> **Không node nào write vào Neo4j.**
> Lịch sử bản dịch (nếu cần) → lưu PostgreSQL từ backend sau khi nhận output.
