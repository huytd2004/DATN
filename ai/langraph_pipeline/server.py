from fastapi import FastAPI, Form, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Any, Dict
import json


try:
    from .tutor_pipeline import TutorPipeline
except ImportError:
    from tutor_pipeline import TutorPipeline

app = FastAPI(title="Tutor Pipeline API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class TargetWord(BaseModel):
    id: Optional[str]
    surface: str
    reading: Optional[str]
    meaning: Optional[str]


class HistoryMessage(BaseModel):
    role: str
    content: str


class ReplyRequest(BaseModel):
    session_meta: Dict[str, Any]
    target_words: Optional[List[TargetWord]] = []
    history: Optional[List[HistoryMessage]] = []
    user_utterance: str


# instantiate pipeline (uses Mock adapters by default)
pipeline = TutorPipeline()


@app.post('/v1/tutor/{session_id}/initial')
def initial_message(
    session_id: str,
    scenario: Optional[str] = Form(None),
    level: Optional[str] = Form(None),
    target_words: Optional[str] = Form(None),
):
    import json
    tw = []
    if target_words:
        try:
            tw = json.loads(target_words)
        except Exception:
            tw = []
    # Pass scenario as-is (None/'auto' will trigger auto-selection in pipeline)
    meta = {'session_id': session_id, 'scenario_name': scenario or 'auto', 'level': level or 'N5'}
    out = pipeline.generate_reply(meta, tw, [], '')
    tts = out.get('tts_filename')
    audio_url = f"/api/v1/tutor/audio/{session_id}/{tts}" if tts else None
    return {
        'message': {
            'id': None,
            'role': 'assistant',
            'content': out.get('contentJa') or out.get('content'),
            'contentJa': out.get('contentJa') or out.get('content'),
            'contentVn': out.get('contentVn') or '',
            'audioUrl': audio_url,
            'corrections': out.get('corrections') or [],
            'newVocabulary': out.get('newVocabulary') or [],
            'suggestions': out.get('suggestions') or [],
        }
    }


@app.post('/v1/tutor/{session_id}/reply')
async def reply(session_id: str, request: Request):
    content_type = (request.headers.get('content-type') or '').lower()

    payload: Dict[str, Any] = {}
    if 'application/json' in content_type:
        try:
            body = await request.json()
            if isinstance(body, dict):
                payload = body
        except Exception:
            payload = {}
    else:
        form = await request.form()
        payload = dict(form)

    metadata = payload.get('metadata')
    if isinstance(metadata, str):
        try:
            parsed_metadata = json.loads(metadata)
            if isinstance(parsed_metadata, dict):
                payload = {**parsed_metadata, **payload}
        except Exception:
            pass

    session_meta = payload.get('session_meta')
    if isinstance(session_meta, str):
        try:
            parsed_session_meta = json.loads(session_meta)
            if isinstance(parsed_session_meta, dict):
                payload['session_meta'] = parsed_session_meta
        except Exception:
            pass

    user_utterance = payload.get('user_utterance') or payload.get('content') or payload.get('text') or ''
    if not isinstance(user_utterance, str):
        user_utterance = str(user_utterance)

    target_words = payload.get('target_words')
    history = payload.get('history')

    # parse optional JSON strings for target_words/history if provided
    try:
        tw = []
        if target_words:
            tw = json.loads(target_words)
        hist = []
        if history:
            hist = json.loads(history)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid JSON in target_words/history: {e}")

    normalized_history = []
    for item in hist:
        if isinstance(item, dict) and 'role' in item and 'content' in item:
            normalized_history.append(item)
            continue
        if isinstance(item, str):
            role = 'user'
            content = item
            if ': ' in item:
                prefix, body = item.split(': ', 1)
                if prefix in ('user', 'assistant', 'system'):
                    role = prefix
                    content = body
            normalized_history.append({'role': role, 'content': content})
            continue
        normalized_history.append({'role': 'user', 'content': str(item)})

    meta = {'session_id': session_id}
    if isinstance(payload.get('session_meta'), dict):
        meta.update(payload['session_meta'])

    out = pipeline.generate_reply(meta, tw, normalized_history, user_utterance)
    tts = out.get('tts_filename')
    audio_url = None
    if tts:
        audio_url = f"/api/v1/tutor/audio/{session_id}/{tts}"

    return {
        'message': {
            'id': None,
            'role': 'assistant',
            'content': out.get('contentJa') or out.get('content'),
            'contentJa': out.get('contentJa') or out.get('content'),
            'contentVn': out.get('contentVn') or '',
            'audioUrl': audio_url,
            'corrections': out.get('corrections'),
            'newVocabulary': out.get('newVocabulary'),
            'suggestions': out.get('suggestions'),
        },
        'transcript': user_utterance,
        'pronunciation': out.get('pronunciation'),
    }


if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host='0.0.0.0', port=8001, reload=False)
