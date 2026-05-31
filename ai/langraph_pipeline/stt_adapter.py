import os
import tempfile
from typing import Optional


class STTAdapter:
    """Abstract STT adapter interface for the pipeline.

    Implementations should provide `transcribe(audio: Optional[bytes]=None, path:Optional[str]=None)`
    returning a UTF-8 string transcript or empty string when transcription unavailable.
    """

    def transcribe(self, audio: Optional[bytes] = None, path: Optional[str] = None) -> str:
        raise NotImplementedError()


class MockSTTAdapter(STTAdapter):
    def transcribe(self, audio: Optional[bytes] = None, path: Optional[str] = None) -> str:
        return ""


class OpenAIWhisperAdapter(STTAdapter):
    """Uses OpenAI Whisper (server-side) via `openai` package if available.

    Expects `audio` as bytes or `path` pointing to a file.
    Requires OPENAI_API_KEY in env or configured via `openai` package.
    """

    def __init__(self):
        try:
            import openai
            self.openai = openai
        except Exception:
            self.openai = None

    def transcribe(self, audio: Optional[bytes] = None, path: Optional[str] = None) -> str:
        if self.openai is None:
            raise RuntimeError('openai package not available')
        # write bytes to temp file if provided
        tmp_path = None
        try:
            if audio is not None:
                import tempfile
                fd, tmp_path = tempfile.mkstemp(suffix='.webm')
                os.close(fd)
                with open(tmp_path, 'wb') as f:
                    f.write(audio)
                fileobj = open(tmp_path, 'rb')
            elif path is not None:
                fileobj = open(path, 'rb')
            else:
                return ''

            # use OpenAI whisper transcription endpoint if available
            try:
                resp = self.openai.Audio.transcribe('whisper-1', fileobj)
                text = getattr(resp, 'text', None) or resp.get('text') if isinstance(resp, dict) else str(resp)
                return text or ''
            except Exception:
                # fallback for older SDKs
                try:
                    resp = self.openai.Transcription.create(file=fileobj, model='whisper-1')
                    return resp.get('text', '') if isinstance(resp, dict) else ''
                except Exception:
                    return ''
        finally:
            try:
                if tmp_path and os.path.exists(tmp_path):
                    os.remove(tmp_path)
            except Exception:
                pass


class LocalWhisperAdapter(STTAdapter):
    """Uses local `whisper` package if installed (e.g., openai/whisper or whisperx).

    This implementation is opportunistic and returns empty string if whisper
    is not available.
    """

    def __init__(self, model: str = 'small'):
        self.model_name = model
        try:
            import whisper
            self.whisper = whisper
            try:
                self.model = whisper.load_model(model)
            except Exception:
                self.model = None
        except Exception:
            self.whisper = None
            self.model = None

    def transcribe(self, audio: Optional[bytes] = None, path: Optional[str] = None) -> str:
        if self.whisper is None:
            return ''
        tmp_path = None
        try:
            if audio is not None:
                import tempfile
                fd, tmp_path = tempfile.mkstemp(suffix='.webm')
                os.close(fd)
                with open(tmp_path, 'wb') as f:
                    f.write(audio)
                src = tmp_path
            elif path is not None:
                src = path
            else:
                return ''

            model = self.model
            if model is None:
                try:
                    model = self.whisper.load_model(self.model_name)
                except Exception:
                    return ''

            res = model.transcribe(src)
            return res.get('text', '') if isinstance(res, dict) else ''
        finally:
            try:
                if tmp_path and os.path.exists(tmp_path):
                    os.remove(tmp_path)
            except Exception:
                pass


class FasterWhisperAdapter(STTAdapter):
    """Uses the local `faster-whisper` package if installed.

    This adapter lazily loads the model on the first transcription call so the
    pipeline can start even when the dependency is present but the model has not
    been downloaded yet.
    """

    def __init__(self, model: str = 'small', device: str = 'cpu', compute_type: str = 'int8'):
        self.model_name = model
        self.device = device
        self.compute_type = compute_type
        self._whisper_module = None
        self._model = None

    def _load_model(self):
        if self._model is not None:
            return self._model

        if self._whisper_module is None:
            try:
                from faster_whisper import WhisperModel
            except Exception:
                return None
            self._whisper_module = WhisperModel

        try:
            self._model = self._whisper_module(
                self.model_name,
                device=self.device,
                compute_type=self.compute_type,
            )
        except Exception:
            self._model = None

        return self._model

    def transcribe(self, audio: Optional[bytes] = None, path: Optional[str] = None) -> str:
        model = self._load_model()
        if model is None:
            return ''

        tmp_path = None
        try:
            if audio is not None:
                fd, tmp_path = tempfile.mkstemp(suffix='.webm')
                os.close(fd)
                with open(tmp_path, 'wb') as file_handle:
                    file_handle.write(audio)
                src = tmp_path
            elif path is not None:
                src = path
            else:
                return ''

            try:
                segments, _info = model.transcribe(src)
            except Exception:
                return ''

            parts = []
            for segment in segments:
                text = getattr(segment, 'text', '')
                if text:
                    parts.append(text)
            return ''.join(parts).strip()
        finally:
            try:
                if tmp_path and os.path.exists(tmp_path):
                    os.remove(tmp_path)
            except Exception:
                pass
