import os
from dotenv import load_dotenv

# Ensure module-local .env is loaded (helps when running from project root)
load_dotenv(os.path.join(os.path.dirname(__file__), '.env'))

try:
    import openai
except Exception:
    openai = None

try:
    import google.genai as genai
except Exception:
    try:
        from google import genai
    except Exception:
        genai = None

try:
    from .config import OPENAI_API_KEY
except ImportError:
    from config import OPENAI_API_KEY


class LLMClient:
    """Simple LLM client supporting Gemini and OpenAI."""

    def __init__(self, api_key=None, provider=None):
        self.provider = (provider or os.getenv('LLM_PROVIDER') or 'gemini').lower()
        self.default_model = os.getenv('LLM_MODEL')
        self._init_error = None

        if self.provider == 'openai':
            self.api_key = api_key or OPENAI_API_KEY or os.getenv('OPENAI_API_KEY')
        else:
            self.api_key = api_key or os.getenv('GOOGLE_API_KEY')

        self._client = None
        self._client_name = None
        if self.provider == 'openai' and openai and self.api_key:
            openai.api_key = self.api_key
            self._client = openai
            self._client_name = 'openai'
        elif self.provider in ('gemini', 'google') and genai:
            try:
                self._client = genai.Client(api_key=self.api_key) if self.api_key else genai.Client()
                self._client_name = 'genai'
            except Exception:
                self._client = None
                self._client_name = None
                self._init_error = 'genai client init failed'
        elif self.provider in ('gemini', 'google') and not genai:
            self._init_error = 'genai import failed'
        elif self.provider == 'openai' and (not openai or not self.api_key):
            self._init_error = 'openai client missing or API key unavailable'

    def complete(self, prompt, model=None, max_tokens=512, temperature=0.2):
        model = model or self.default_model

        if self._client_name == 'genai' and self._client is not None:
            model = model or 'gemini-2.0-flash'
            try:
                resp = self._client.models.generate_content(
                    model=model,
                    contents=prompt,
                    config={
                        'temperature': temperature,
                        'max_output_tokens': max_tokens,
                    },
                )
                text = getattr(resp, 'text', None) or str(resp)
                return {'text': text, 'raw': resp}
            except Exception as e:
                return {'text': f'[[Gemini error]] {e} {prompt[:200]}'}

        if self._client_name == 'openai' and self._client is not None:
            model = model or 'gpt-4o-mini'
            try:
                resp = openai.ChatCompletion.create(
                    model=model,
                    messages=[{'role': 'user', 'content': prompt}],
                    max_tokens=max_tokens,
                    temperature=temperature,
                )
                text = resp['choices'][0]['message']['content']
                return {'text': text, 'raw': resp}
            except Exception as e:
                return {'text': f'[[OpenAI error]] {e} {prompt[:200]}'}

        suffix = f' ({self._init_error})' if self._init_error else ''
        return {'text': '[[LLM unavailable]]' + suffix + ' ' + prompt[:200]}
