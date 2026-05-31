try:
    from .llm_client import LLMClient
    from .stt_adapter import STTAdapter, MockSTTAdapter, OpenAIWhisperAdapter, LocalWhisperAdapter, FasterWhisperAdapter
    from .tts_adapter import TTSAdapter, MockTTSAdapter
except ImportError:
    from llm_client import LLMClient
    from stt_adapter import STTAdapter, MockSTTAdapter, OpenAIWhisperAdapter, LocalWhisperAdapter, FasterWhisperAdapter
    from tts_adapter import TTSAdapter, MockTTSAdapter
import os
import re
import random

# ── Auto-scenario keyword map ─────────────────────────────────────────────────
# Keys = scenario name. Values = Japanese surfaces/readings typically associated.
_AUTO_SCENARIO_KEYWORDS: dict = {
    'restaurant': [
        '食べる', '飲む', '注文', 'メニュー', '料理', 'レストラン', '食事', '美味しい',
        '召し上がる', '食べ物', 'ご飯', 'ランチ', 'ディナー', '朝食', '昼食', '夕食',
        'お水', '水', 'コーヒー', 'お茶', 'ジュース', 'ビール', 'ワイン',
        '肉', '魚', '野菜', 'ラーメン', '寿司', 'そば', 'うどん', 'カレー',
        '辛い', '甘い', '塩', 'すっぱい', 'お腹', '満腹', '腹', 'デザート',
        '割り勘', '会計', 'レシート', 'おすすめ',
    ],
    'shopping': [
        '買う', '値段', '安い', '高い', 'お金', 'ショッピング', '店', '商品', 'セール',
        '購入', '売る', 'デパート', 'スーパー', 'コンビニ', 'マーケット',
        '円', '払う', 'おつり', 'カード', '財布', '袋', '荷物',
        '服', 'シャツ', 'ズボン', 'くつ', '帽子', 'バッグ', 'かばん',
        '本', '雑誌', '文房具', 'ノート', 'ペン', 'えんぴつ',
        '電化製品', 'スマホ', 'パソコン', '電池',
        'サイズ', '色', '形', '試着', '返品', '交換',
    ],
    'travel': [
        '旅行', '電車', '駅', 'ホテル', '観光', '地図', '切符', 'バス', '空港', '予約',
        '飛行機', '船', '車', 'タクシー', '新幹線', '地下鉄',
        '荷物', 'スーツケース', 'パスポート', 'チケット', 'ビザ',
        '観光地', '名所', '博物館', '美術館', '公園', '海', '山', '川',
        '温泉', '旅館', 'ryokan', 'チェックイン', 'チェックアウト', '部屋',
        '地図', '道', '右', '左', '直進', '曲がる', 'どこ', '場所',
        '写真', '撮る', 'お土産', '記念品',
    ],
    'interview': [
        '仕事', '会社', '面接', '経験', 'スキル', 'キャリア', '申し込む', '入社', '志望',
        '職業', '社員', '上司', '同僚', '給料', '残業', '休日', '有給',
        '大学', '専攻', '卒業', '留学', '資格', '免許',
        '自己紹介', '長所', '短所', '目標', '将来', '夢',
        '働く', '勤める', 'アルバイト', 'パート', '正社員',
    ],
    'school': [
        '勉強', '学校', '授業', '先生', '生徒', '学生', 'クラス', 'テスト', '試験',
        '宿題', 'レポート', '本', '教科書', 'ノート', 'えんぴつ', 'ペン',
        '科目', '数学', '英語', '日本語', '理科', '社会', '体育', '音楽',
        '図書館', '教室', 'キャンパス', '大学', '高校', '中学', '小学',
        '成績', '点数', '合格', '不合格', '卒業', '入学',
        '読む', '書く', '聞く', '話す', '覚える', '理解する',
    ],
    'daily': [
        '今日', '明日', '昨日', '天気', '友達', '家族', '毎日', '時間', '好き',
        '起きる', '寝る', '朝', '昼', '夜', '晩', '週末', '休み',
        '家', '部屋', 'リビング', 'キッチン', 'お風呂', 'トイレ',
        '犬', '猫', 'ペット', '花', '植物',
        '映画', '音楽', '読書', 'ゲーム', 'スポーツ', 'テレビ',
        '電話', 'メール', 'SNS', 'インターネット',
        '病気', '薬', '病院', '医者', '健康',
        '誕生日', 'パーティー', '祝う', 'プレゼント',
    ],
}

# Vietnamese meaning keywords → scenario hints
_MEANING_HINTS: dict = {
    'restaurant': ['ăn', 'uống', 'đặt món', 'nhà hàng', 'thức ăn', 'đồ ăn', 'bữa', 'thực đơn',
                   'nước', 'cơm', 'phở', 'ramen', 'sushi', 'trà', 'cà phê', 'bia'],
    'shopping':   ['mua', 'bán', 'giá', 'rẻ', 'đắt', 'tiền', 'cửa hàng', 'siêu thị',
                   'sách', 'quần áo', 'giày', 'túi', 'máy tính', 'điện thoại'],
    'travel':     ['du lịch', 'tàu', 'ga', 'khách sạn', 'máy bay', 'vé', 'bản đồ',
                   'taxi', 'biển', 'núi', 'tham quan', 'ảnh', 'quà'],
    'interview':  ['công việc', 'công ty', 'phỏng vấn', 'kinh nghiệm', 'kỹ năng',
                   'nghề', 'lương', 'nhân viên', 'tốt nghiệp', 'đại học'],
    'school':     ['học', 'trường', 'bài tập', 'giáo viên', 'học sinh', 'thi', 'điểm',
                   'sách giáo khoa', 'bút', 'vở', 'thư viện', 'lớp'],
    'daily':      ['hàng ngày', 'gia đình', 'bạn bè', 'thời tiết', 'nhà', 'ngủ', 'dậy',
                   'buổi sáng', 'phim', 'âm nhạc', 'chó', 'mèo', 'sinh nhật'],
}


def _auto_select_scenario(target_words: list) -> str:
    """Chọn chủ đề phù hợp nhất dựa trên target_words (surface, reading, meaning)."""
    surfaces  = {w.get('surface', '').strip() for w in (target_words or [])}
    readings  = {w.get('reading', '').strip() for w in (target_words or [])}
    meanings  = ' '.join(w.get('meaning', '') for w in (target_words or [])).lower()

    scores: dict = {}
    for scenario, kws in _AUTO_SCENARIO_KEYWORDS.items():
        # Count Japanese surface/reading matches
        ja_score = sum(1 for kw in kws if kw in surfaces or kw in readings)
        # Count Vietnamese meaning hint matches
        vn_score = sum(1 for hint in _MEANING_HINTS.get(scenario, []) if hint in meanings)
        scores[scenario] = ja_score * 2 + vn_score  # Japanese match weighted higher

    best = max(scores, key=scores.get)
    return best if scores[best] > 0 else random.choice(list(_AUTO_SCENARIO_KEYWORDS.keys()))

import json
from typing import Any
from typing import List, Dict, Optional


class TutorPipeline:
    """Simple scaffold for the Tutor pipeline using existing LLM client.

    This module is intentionally minimal: it builds a structured prompt including
    session context, target words and recent history, then calls the LLM client
    to produce a tutor reply. It also exposes hooks for STT/TTS integration.
    """

    def __init__(self, provider_api_key: Optional[str] = None, provider: Optional[str] = None, llm_client: Optional[LLMClient] = None):
        # allow injection of a fake LLM client for tests
        self.llm = llm_client or LLMClient(api_key=provider_api_key, provider=provider)
        # Decide STT adapter: explicit injection not implemented here; choose by env var
        stt_provider = os.getenv('STT_PROVIDER', '').lower()
        if stt_provider == 'openai':
            try:
                self.stt = OpenAIWhisperAdapter()
            except Exception:
                self.stt = MockSTTAdapter()
        elif stt_provider in ('faster-whisper', 'faster_whisper'):
            try:
                self.stt = FasterWhisperAdapter(
                    model=os.getenv('FASTER_WHISPER_MODEL', 'small'),
                    device=os.getenv('FASTER_WHISPER_DEVICE', 'cpu'),
                    compute_type=os.getenv('FASTER_WHISPER_COMPUTE_TYPE', 'int8'),
                )
            except Exception:
                self.stt = MockSTTAdapter()
        elif stt_provider == 'local':
            try:
                self.stt = LocalWhisperAdapter()
            except Exception:
                self.stt = MockSTTAdapter()
        else:
            self.stt: STTAdapter = MockSTTAdapter()
        self.tts: TTSAdapter = MockTTSAdapter()

    def build_prompt(self, session_meta: Dict, target_words: List[Dict], history: List[Dict], user_utterance: str) -> str:
        # ── Auto-select scenario ──────────────────────────────────────────────
        scenario = (session_meta.get('scenario_name') or '').strip().lower()
        if not scenario or scenario in ('auto', 'general', 'null'):
            scenario = _auto_select_scenario(target_words)

        header = (
            f"You are Sakura-sensei, a warm and encouraging Japanese conversation tutor.\n"
            f"Scenario: {scenario}. Level: {session_meta.get('level', 'N5')}.\n"
            f"Always respond in Japanese appropriate for the level, then give a short Vietnamese note if needed.\n"
            f"IMPORTANT: Do NOT use markdown formatting (no **, *, _, __, ##, etc.) in content_ja. Plain text only.\n"
        )

        words_block = ""
        if target_words:
            words_block = "Target vocabulary to practice:\n"
            for w in target_words:
                words_block += f"- {w.get('surface', '')} ({w.get('reading', '')}) — {w.get('meaning', '')}\n"
            words_block += "Try to naturally incorporate these words into the conversation.\n"

        history_block = ""
        recent = (history or [])[-5:]
        if recent:
            history_block = "Recent conversation:\n"
            for m in recent:
                history_block += f"{m.get('role', 'user')}: {m.get('content', '')}\n"

        # ── Initial message (no utterance yet) ───────────────────────────────
        if not user_utterance or not user_utterance.strip():
            prompt = (
                header + "\n" + words_block + "\n"
                + f"This is the START of a '{scenario}' conversation practice session.\n"
                + "Write a SHORT greeting (1-2 sentences max) IN JAPANESE, then ask ONE simple practice question. "
                + "Provide 2-3 short Japanese suggestions the student could reply with.\n"
                + "Keep content_ja under 60 words total.\n"
                + "Return ONLY a JSON object with these exact fields:\n"
                + "{\"content_ja\": \"<greeting + one question, plain Japanese, no markdown>\",\n"
                + " \"content_vn\": \"<short Vietnamese translation of content_ja>\",\n"
                + " \"suggestions\": [\"<suggestion1 in Japanese>\", \"<suggestion2 in Japanese>\", \"<suggestion3 in Japanese>\"],\n"
                + " \"corrections\": [], \"newVocabulary\": []}\n"
            )
            return prompt

        # ── Regular reply ─────────────────────────────────────────────────────
        prompt = (
            header + "\n" + words_block + "\n" + history_block + "\n"
            + f"Student says: {user_utterance}\n\n"
            + "STEP 1 — GRAMMAR CHECK (MANDATORY):\n"
            + "Carefully analyze the student's sentence for ANY Japanese errors, including:\n"
            + "- Wrong verb conjugation (e.g. 「買いますたいです」→ WRONG, correct is 「買いたいです」; たい must attach to verb STEM, not ます form)\n"
            + "- Particle misuse (e.g. を/が/に/で/は confusion)\n"
            + "- Wrong tense or politeness level\n"
            + "- Word order mistakes\n"
            + "- Any unnatural Japanese phrasing\n"
            + "If you find ANY error, you MUST add it to the corrections array with: original, corrected, and a brief note in Vietnamese.\n"
            + "If the sentence is perfectly correct, set corrections to [].\n\n"
            + "STEP 2 — REPLY:\n"
            + "1. Continue the conversation naturally in Japanese.\n"
            + "2. Ask a follow-up question to keep practicing.\n"
            + "3. Suggest 2-3 short Japanese replies the student could use next.\n"
            + "4. Add new vocabulary introduced in your reply (if any).\n\n"
            + "Return ONLY a JSON object with these exact fields:\n"
            + "{\"content_ja\": \"<your Japanese response + follow-up question ONLY, no Vietnamese here>\",\n"
            + " \"content_vn\": \"<Short Vietnamese translation/explanation of content_ja>\",\n"
            + " \"corrections\": [{\"original\": \"<student's wrong phrase>\", \"corrected\": \"<correct form>\", \"note\": \"<brief Vietnamese explanation>\"}],\n"
            + " \"suggestions\": [\"<ja reply 1>\", \"<ja reply 2>\", \"<ja reply 3>\"],\n"
            + " \"newVocabulary\": [{\"surface\": \"...\", \"reading\": \"...\", \"meaning\": \"...\"}]}\n"
        )
        return prompt

    def generate_reply(self, session_meta: Dict, target_words: List[Dict], history: List[Dict], user_utterance: str) -> Dict:
        prompt = self.build_prompt(session_meta, target_words, history, user_utterance)
        resp = self.llm.complete(prompt, max_tokens=3000, temperature=0.2)
        text = resp.get('text') if isinstance(resp, dict) else str(resp)
        # Try to extract structured JSON from the LLM text response.
        content_ja = ''
        content_vn = ''
        corrections = []
        suggestions = []
        new_vocab = []

        def try_extract_json(s: str) -> Any:
            # Remove common code fences (```json ... ``` or ``` ... ```)
            s_clean = re.sub(r'```(?:json)?\n?', '', s)
            s_clean = re.sub(r'```\s*$', '', s_clean).strip()

            # Try 1: parse the entire cleaned string directly
            try:
                return json.loads(s_clean)
            except Exception:
                pass

            # Try 2: find the LARGEST {...} block (greedy) to get the outermost object
            for pat in (r"\{[\s\S]*\}", r"\[[\s\S]*\]"):
                m = re.search(pat, s_clean)
                if m:
                    chunk = m.group(0)
                    try:
                        return json.loads(chunk)
                    except Exception:
                        try:
                            alt = chunk.replace("'", '"')
                            return json.loads(alt)
                        except Exception:
                            pass

            # Try 3: attempt YAML if PyYAML is installed
            try:
                import yaml
                try:
                    parsed = yaml.safe_load(s_clean)
                    if isinstance(parsed, (dict, list)):
                        return parsed
                except Exception:
                    pass
            except Exception:
                pass

            return None

        parsed = try_extract_json(text)
        if isinstance(parsed, dict):
            content_ja = parsed.get('content_ja') or parsed.get('content') or text
            content_vn = parsed.get('content_vn') or ''
            corrections = parsed.get('corrections') or []
            suggestions = parsed.get('suggestions') or []
            raw_vocab = parsed.get('newVocabulary') or parsed.get('new_vocabulary') or []
        else:
            # JSON parse failed (likely truncated response) — extract fields with regex
            # Regex does NOT require closing quote so it works on truncated strings too
            clean = re.sub(r'```(?:json)?\n?', '', text).strip()
            ja_match = re.search(r'"content_ja"\s*:\s*"((?:[^"\\]|\\.)*)', clean)
            vn_match = re.search(r'"content_vn"\s*:\s*"((?:[^"\\]|\\.)*?)"', clean)
            content_ja = ja_match.group(1).rstrip('\\') if ja_match else clean
            content_vn = vn_match.group(1) if vn_match else ''
            raw_vocab = []

        # Deduplicate newVocabulary: remove words already present in target_words
        target_surfaces = {w.get('surface', '').strip() for w in (target_words or [])}
        new_vocab = [
            v for v in raw_vocab
            if isinstance(v, dict) and v.get('surface', '').strip() not in target_surfaces
        ]

        # TTS only uses the Japanese content
        tts_filename = None
        session_id = session_meta.get('id') or session_meta.get('session_id')
        if session_id:
            out_dir = os.path.join(os.getcwd(), 'uploads', 'tutor-audio', str(session_id))
            tts_filename = self.tts.synthesize(content_ja, out_dir=out_dir)

        return {
            'contentJa': content_ja,
            'contentVn': content_vn,
            'content': content_ja,  # backward-compat
            'corrections': corrections,
            'suggestions': suggestions,
            'newVocabulary': new_vocab,
            'tts_filename': tts_filename,
        }


if __name__ == '__main__':
    # Quick local test
    p = TutorPipeline()
    out = p.generate_reply({'scenario_name':'restaurant','level':'N4'},{'dummy':1}, [{'role':'user','content':'こんにちは'}], 'ラーメンを一つください')
    print(out)
