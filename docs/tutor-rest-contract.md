# Tutor REST Contract

This document describes the temporary REST boundary between the Java backend and the Python AI layer.

## Base URL

- Python AI service defaults to `http://localhost:8001`.
- Java backend config: `app.tutor.ai-base-url` in `backend/src/main/resources/application.yaml`.

## Endpoints

### `POST /v1/tutor/{session_id}/initial`

Form fields:

- `scenario`
- `level`
- `session_id`

Response shape:

```json
{
  "message": {
    "id": "uuid",
    "role": "assistant",
    "content": "...",
    "audioUrl": null,
    "corrections": [],
    "newVocabulary": [],
    "suggestions": []
  }
}
```

### `POST /v1/tutor/{session_id}/reply`

Accepted payloads:

- form-data / x-www-form-urlencoded
- JSON body

Fields:

- `user_utterance` or `content`
- `target_words` as JSON string
- `history` as JSON string
- optional `session_meta` as JSON object/string
- optional `audio` file

Response shape:

```json
{
  "message": {
    "id": "uuid",
    "role": "assistant",
    "content": "...",
    "audioUrl": null,
    "corrections": [],
    "newVocabulary": [],
    "suggestions": []
  },
  "transcript": "...",
  "pronunciation": null
}
```

## Browser TTS

- Backend does not synthesize assistant audio when `app.tutor.browser-tts=true`.
- Frontend `MessageBubble.vue` uses the browser `SpeechSynthesis` API for playback.
