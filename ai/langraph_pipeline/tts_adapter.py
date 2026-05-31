from typing import Optional
import os


class TTSAdapter:
    """Abstract TTS adapter interface."""

    def synthesize(self, text: str, out_dir: Optional[str] = None) -> str:
        raise NotImplementedError()


class MockTTSAdapter(TTSAdapter):
    def synthesize(self, text: str, out_dir: Optional[str] = None) -> str:
        # Write a small placeholder file and return its filename
        out_dir = out_dir or os.getcwd()
        os.makedirs(out_dir, exist_ok=True)
        fname = f"mock_tts_{int(__import__('time').time())}.txt"
        path = os.path.join(out_dir, fname)
        with open(path, 'w', encoding='utf-8') as f:
            f.write('TTS placeholder for: ' + text)
        return fname
