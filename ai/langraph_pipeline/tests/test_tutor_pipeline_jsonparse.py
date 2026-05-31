class FakeLLM:
    def __init__(self, text):
        self._text = text

    def complete(self, prompt, **kwargs):
        return {'text': self._text}


def test_parse_structured_json(tmp_path):
    from ai.langraph_pipeline.tutor_pipeline import TutorPipeline
    # craft an LLM response that contains JSON after some commentary
    llm_text = "Here is the reply.\n```json\n{\n  \"content\": \"いいですね\",\n  \"corrections\": [{\"type\": \"grammar\", \"original\": \"ラーメン一つください\", \"corrected\": \"ラーメンを一つください\", \"explanation\": \"助詞を付ける\"}],\n  \"suggestions\": [\"お水もください\"],\n  \"newVocabulary\": [{\"surface\": \"会計\", \"reading\": \"かいけい\", \"meaning\": \"thanh toán\"}]\n}\n```"

    tp = TutorPipeline(llm_client=FakeLLM(llm_text))
    out = tp.generate_reply({'session_id': 's1'}, [], [], 'ユーザ発話')
    assert out['content'] is not None
    assert isinstance(out['corrections'], list)
    assert len(out['corrections']) == 1
    assert out['suggestions'] == ['お水もください']
    assert isinstance(out['newVocabulary'], list) and len(out['newVocabulary']) == 1
