import os
import shutil
from ai.langraph_pipeline.tutor_pipeline import TutorPipeline


def test_generate_reply_creates_tts(tmp_path):
    tp = TutorPipeline()
    session_meta = {'session_id': 'test-session', 'scenario_name': 'restaurant', 'level': 'N4'}
    target_words = [{'surface': '注文', 'reading': 'ちゅうもん', 'meaning': 'gọi món'}]
    history = [{'role': 'user', 'content': 'こんにちは'}]
    out = tp.generate_reply(session_meta, target_words, history, 'ラーメンを一つください')
    # tts_filename should be present and file created under uploads/tutor-audio/test-session
    fname = out.get('tts_filename')
    assert fname is not None
    path = os.path.join(os.getcwd(), 'uploads', 'tutor-audio', 'test-session', fname)
    assert os.path.exists(path)
    # cleanup
    shutil.rmtree(os.path.join(os.getcwd(), 'uploads', 'tutor-audio', 'test-session'))


def test_selects_faster_whisper_adapter(monkeypatch):
    import sys
    import types

    class FakeSegment:
        def __init__(self, text):
            self.text = text

    class FakeModel:
        def __init__(self, *args, **kwargs):
            self.args = args
            self.kwargs = kwargs

        def transcribe(self, src):
            return [FakeSegment('xin chào '), FakeSegment('thế giới')], None

    fake_module = types.SimpleNamespace(WhisperModel=FakeModel)
    monkeypatch.setenv('STT_PROVIDER', 'faster-whisper')
    monkeypatch.setenv('FASTER_WHISPER_MODEL', 'tiny')
    monkeypatch.setitem(sys.modules, 'faster_whisper', fake_module)

    tp = TutorPipeline()
    assert tp.stt.__class__.__name__ == 'FasterWhisperAdapter'
    assert tp.stt.transcribe(path='/tmp/fake.wav') == 'xin chào thế giới'
