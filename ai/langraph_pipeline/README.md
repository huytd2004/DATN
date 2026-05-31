# Tutor Pipeline service

Run a small FastAPI wrapper for the `TutorPipeline` used by the backend during development.

Install (prefer virtualenv) and deps:

```bash
cd ai/langraph_pipeline
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Run the server:

```bash
cd ai/langraph_pipeline
uvicorn server:app --host 0.0.0.0 --port 8001
```

If you want to run it from the repository root instead, use:

```bash
uvicorn ai.langraph_pipeline.server:app --host 0.0.0.0 --port 8001
```

Endpoints:
- `POST /v1/tutor/{session_id}/initial` — form fields `scenario`, `level`.
- `POST /v1/tutor/{session_id}/reply` — accepts form-data or JSON; use `user_utterance` or `content`, optional JSON strings `target_words`, `history`, optional `session_meta`, optional file `audio`.

Responses include `message` with `content`, `corrections`, `suggestions`, `newVocabulary` and `audioUrl` if TTS produced a file under `uploads/tutor-audio/{session_id}`.

STT provider
-----------
You can configure the STT provider via environment variable `STT_PROVIDER`:

- `STT_PROVIDER=openai` — use OpenAI Whisper (requires `openai` package and API key in env).
- `STT_PROVIDER=faster-whisper` — use local Faster-Whisper (requires `faster-whisper` and system `ffmpeg`).
- `STT_PROVIDER=local` — use local `whisper` package if installed.
- (default) uses mock adapter returning empty transcript.

Optional Faster-Whisper settings:

- `FASTER_WHISPER_MODEL` — model name, default `small`
- `FASTER_WHISPER_DEVICE` — `cpu` or `cuda`, default `cpu`
- `FASTER_WHISPER_COMPUTE_TYPE` — for example `int8` on CPU

Langraph pipeline scaffold for GraphRAG+LLM translation.

Usage:
- Create a virtualenv and install requirements: `pip install -r requirements.txt`
- Configure environment variables in `.env`:
  - `NEO4J_URI`, `NEO4J_USER`, `NEO4J_PASS`, `OPENAI_API_KEY`
- Run example: `python -m langraph_pipeline.runner --sentenceId S1 --text "..."`

Files:
- `neo4j_client.py`: Neo4j access helpers
- `ranker.py`: candidate scoring
- `prompt_builder.py`: builds prompt for LLM
- `llm_client.py`: OpenAI wrapper (fallback when no key)
- `runner.py`: orchestrator CLI

Next steps:
- Improve candidate Cypher queries for multi-token collocations
- Add unit tests and CI
- Integrate real LangGraph SDK if required by project
