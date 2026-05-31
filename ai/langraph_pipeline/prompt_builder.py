def build_prompt(input_text: str, ranked_senses: list[dict], detected_domains: list[str]) -> str:
    """
    Tạo structured prompt cho LLM từ ranked_senses và detected_domains.
    Output yêu cầu LLM trả về JSON với translation + notes.
    """
    domains_str = ", ".join(detected_domains) if detected_domains else "general"

    evidence_lines = []
    for rs in ranked_senses:
        top = rs['top_sense']
        line = (
            f"- Token: {rs['surface']}"
            f" → Sense: {top.get('glossVi', '?')}"
            f" | Domain: {top.get('domain', '?')}"
            f" | Register: {top.get('register', '?')}"
            f" | Score: {rs['score']:.3f}"
        )
        if top.get('culturalNote'):
            line += f"\n  ⚠ Cultural note: {top['culturalNote']}"
        if top.get('usageNote'):
            line += f"\n  📌 Usage: {top['usageNote']}"
        examples = top.get('examples') or []
        if examples and examples[0].get('ja'):
            ex = examples[0]
            line += f"\n  Ví dụ: {ex.get('ja', '')} → {ex.get('vi', '')}"
        evidence_lines.append(line)

    evidence_block = "\n".join(evidence_lines) if evidence_lines else "(không có evidence)"

    prompt = f"""Bạn là dịch giả chuyên ngành Nhật-Việt.
Nhiệm vụ: Dịch đoạn văn tiếng Nhật sang tiếng Việt chính xác và tự nhiên.

Yêu cầu:
- Giữ đúng domain ({domains_str}) và register của từng thuật ngữ.
- Ưu tiên nghĩa theo "Graph evidence" bên dưới.
- Nếu từ đa nghĩa, dùng sense có score cao nhất.
- Không dịch literal từng từ. Diễn đạt tự nhiên theo tiếng Việt.
- Nếu có sắc thái văn hóa, giữ nguyên ý nghĩa văn hóa đó.

Source text:
{input_text}

Graph evidence:
{evidence_block}

Trả về đúng JSON sau (không thêm markdown code block):
{{
  "translation": "<bản dịch tự nhiên>",
  "notes": [
    {{"type": "polysemy|cultural|register|technical", "token": "<từ Nhật>", "content": "<giải thích ngắn gọn>"}}
  ]
}}
"""
    return prompt
