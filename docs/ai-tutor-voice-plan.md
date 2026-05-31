# AI Tutor Voice — Implementation Plan

## 1. Mục tiêu

Xây dựng chức năng **AI Tutor luyện giao tiếp qua voice** cho người học tiếng Nhật. Người học chọn bộ từ vựng hoặc chủ đề, nói chuyện trực tiếp với AI theo tình huống thực tế, nhận phản hồi về phát âm, ngữ pháp, từ vựng, độ tự nhiên của câu nói và xem tổng kết sau phiên học.

Luồng chính:

1. Người học vào `TutorSetupView.vue`.
2. Chọn deck, chủ đề hội thoại, cấp độ JLPT và thời lượng.
3. Frontend tạo session AI Tutor.
4. Người học vào `TutorChatView.vue` và luyện nói bằng microphone.
5. Hệ thống chuyển giọng nói thành text, gửi text + audio metadata cho backend/AI layer.
6. AI phản hồi bằng text và voice, đồng thời trả corrections/suggestions.
7. Người học kết thúc phiên và xem `TutorResultView.vue`.

## 2. Kiến trúc tổng quan

```text
Browser
Vue 3 + Pinia + Web Speech API / MediaRecorder
        |
        | REST API / WebSocket
        v
Spring Boot Backend
Auth + Session + Logs + Tutor API
        |
        | REST/gRPC
        v
Python AI Layer
LangGraph + LLM + STT/TTS adapter
```

PostgreSQL lưu session, message log, target words và kết quả đánh giá. AI layer tạo ngữ cảnh luyện nói từ deck đã chọn, target words, scenario, cấp độ JLPT và lịch sử hội thoại trong session; không dùng GraphRAG hoặc Neo4j cho chức năng này.

## 3. Frontend Scope

### 3.1 Files cần thêm

- `frontend/src/services/tutor.js`
- `frontend/src/stores/tutor.js`
- Có thể thêm component nhỏ nếu cần:
  - `frontend/src/components/tutor/VoiceRecorder.vue`
  - `frontend/src/components/tutor/MessageBubble.vue`
  - `frontend/src/components/tutor/CorrectionCard.vue`

### 3.2 `TutorSetupView.vue`

Chức năng:

- Load decks từ `GET /api/v1/decks`.
- Khi chọn deck, load thẻ đến hạn từ `GET /api/v1/decks/{id}/due?maxNew=10`.
- Cho chọn scenario cố định:
  - `restaurant`: nhà hàng
  - `shopping`: mua sắm
  - `interview`: phỏng vấn
  - `travel`: du lịch
- Cho chọn level: `N5`, `N4`, `N3`, `N2`, `N1`
- Cho chọn thời lượng: `10`, `15`, `20` phút.
- Tạo target words từ due cards:
  - `id`
  - `surface` lấy từ `frontText` hoặc `front_text`
  - `reading` lấy từ `frontReading` hoặc `front_reading`
  - `meaning` lấy từ `backText` hoặc `back_text`
- Quy tắc chọn thẻ cho AI Tutor:
  - Ưu tiên thẻ `LEARNING` và `REVIEW` đã đến hạn trong deck hiện tại.
  - Nếu còn thiếu số lượng ngữ cảnh, lấy thêm thẻ `NEW` theo giới hạn `maxNew`.
  - Không gửi toàn bộ deck vào AI Tutor khi deck quá lớn.
- Nếu deck chứa nhiều thẻ không liên quan tới chủ đề hội thoại:
  - Hiển thị preview target words trước khi bắt đầu để người học kiểm tra.
  - Cho phép loại bỏ thẻ khỏi phiên AI Tutor trước khi tạo session.
  - Nếu phần lớn thẻ trong deck lệch chủ đề, khuyến nghị người học đổi deck hoặc chọn chủ đề khác phù hợp hơn.
- Hiện tại AI sẽ tự suy luận và chọn chủ đề hội thoại dựa trên các thẻ trong deck.
- Sau này, khi admin gắn deck với từng chủ đề cụ thể, người học có thể chọn chủ đề hội thoại thủ công.
- Nút bắt đầu disabled khi thiếu deck/scenario hoặc đang gọi API.
- Gọi `POST /api/v1/tutor/sessions`, lưu session vào Pinia, chuyển sang `/tutor/chat?sessionId=<uuid>`.

UI states:

- Loading skeleton khi tải decks.
- Empty state khi chưa có deck.
- Error banner có nút thử lại.
- Preview target words trước khi bắt đầu.

### 3.3 `TutorChatView.vue`

Chức năng chính:

- Hydrate session từ query `sessionId` hoặc Pinia/sessionStorage.
- Nếu thiếu session, redirect về `/tutor`.
- Render messages theo role:
  - `user`
  - `assistant`
  - `system`
- Hỗ trợ text input để fallback khi microphone lỗi.
- Hỗ trợ voice input:
  - Bấm giữ hoặc toggle record.
  - Dùng `MediaRecorder` để thu audio blob.
  - Dùng Web Speech API nếu browser hỗ trợ để có transcript local nhanh.
  - Nếu Web Speech API không hỗ trợ, gửi audio blob cho backend STT.
- Khi gửi:
  - Optimistic append user message.
  - Hiện trạng thái AI đang nghe/đang suy nghĩ.
  - Gọi API message.
  - Append assistant response.
- Assistant response gồm:
  - `message.content`
  - `corrections`
  - `suggestions`
  - `newVocabulary` — hiển thị tích lũy tại sidebar panel "Từ vựng mới"
- Có quick replies để người học có thể chọn nếu không muốn nói.
- Nút kết thúc phiên gọi finish API, lưu result, chuyển sang `/tutor/result?sessionId=<uuid>`.

Voice UX:

- Trạng thái microphone:
  - idle
  - requesting permission
  - recording
  - transcribing
  - sending
  - error
- TTS: dùng `speechSynthesis` browser làm **ưu tiên chính** (không phụ thuộc `audioUrl` từ backend). Fallback sang `audio.src = audioUrl` nếu browser không hỗ trợ `speechSynthesis`.
- Có nút nghe lại câu AI bằng TTS.
- Có fallback rõ ràng: "Không dùng được microphone, bạn vẫn có thể nhập text."

### 3.4 `TutorResultView.vue`

Chức năng:

- Load result từ Pinia hoặc `GET /api/v1/tutor/sessions/{sessionId}/result`.
- Hiển thị:
  - thời lượng phiên
  - số lượt nói của user
  - số phản hồi của AI
  - fluency score
  - accuracy score
  - pronunciation score
  - estimated level
- Danh sách lỗi/sửa:
  - `type`
  - `original`
  - `corrected`
  - `explanation`
  - `timestamp`
- Từ vựng đã dùng/từ mới:
  - `surface`
  - `reading`
  - `meaning`
  - `source`
  - `usageNote`
- CTA:
  - Học tiếp `/tutor`
  - Dashboard `/dashboard`
  - Tạo flashcard từ từ mới khi backend có endpoint tương ứng.

## 4. Frontend API Contract

### 4.1 `frontend/src/services/tutor.js`

```js
import api from "@/services/api";

export const tutorService = {
  createSession(payload) {
    return api.post("/api/v1/tutor/sessions", payload);
  },

  sendMessage(sessionId, payload) {
    return api.post(`/api/v1/tutor/sessions/${sessionId}/messages`, payload);
  },

  finishSession(sessionId) {
    return api.patch(`/api/v1/tutor/sessions/${sessionId}/finish`);
  },

  getResult(sessionId) {
    return api.get(`/api/v1/tutor/sessions/${sessionId}/result`);
  },
};
```

### 4.2 Create session request

```json
{
  "deckId": "uuid",
  "scenarioName": "restaurant",
  "level": "N3",
  "durationMinutes": 15,
  "targetWords": [
    {
      "id": "uuid",
      "surface": "注文",
      "reading": "ちゅうもん",
      "meaning": "gọi món"
    }
  ]
}
```

### 4.3 Create session response

```json
{
  "sessionId": "uuid",
  "scenarioName": "restaurant",
  "level": "N3",
  "durationMinutes": 15,
  "targetWords": [],
  "initialMessage": {
    "id": "uuid",
    "role": "assistant",
    "content": "こんにちは。今日はレストランで注文する練習をしましょう。",
    "audioUrl": "/api/v1/tutor/audio/..."
  }
}
```

### 4.4 Send voice/text message request

Với text transcript:

```json
{
  "content": "ラーメンを一つください",
  "inputMode": "voice",
  "audioMetadata": {
    "durationMs": 3200,
    "mimeType": "audio/webm"
  }
}
```

Nếu cần upload audio thật, dùng `multipart/form-data`:

- `metadata`: JSON string
- `audio`: audio blob

### 4.5 Send message response

```json
{
  "message": {
    "id": "uuid",
    "role": "assistant",
    "content": "いいですね。「ラーメンを一つください」は自然です。飲み物も注文してみましょう。",
    "audioUrl": "/api/v1/tutor/audio/..."
  },
  "transcript": "ラーメンを一つください",
  "pronunciation": {
    "score": 82,
    "notes": [
      "イントネーションは自然ですが、「ください」をもう少しはっきり発音しましょう。"
    ]
  },
  "corrections": [
    {
      "type": "grammar",
      "original": "ラーメン一つください",
      "corrected": "ラーメンを一つください",
      "explanation": "目的語には助詞「を」を付けると自然です。"
    }
  ],
  "suggestions": ["お水もください", "おすすめは何ですか", "会計をお願いします"],
  "newVocabulary": [
    {
      "surface": "会計",
      "reading": "かいけい",
      "meaning": "thanh toán",
      "source": "session_target_words",
      "usageNote": "Dùng khi muốn thanh toán ở nhà hàng hoặc cửa hàng."
    }
  ]
}
```

## 5. Pinia Store

Store `useTutorStore` cần quản lý:

- `sessionId`
- `selectedDeck`
- `scenario`
- `level`
- `durationMinutes`
- `targetWords`
- `messages`
- `feedback`
- `result`
- `recordingState`
- `lastError`

Actions:

- `hydrate()`
- `persist()`
- `startSession(payload)`
- `appendMessage(message)`
- `sendTextMessage(content)`
- `sendVoiceMessage({ transcript, audioBlob, audioMetadata })`
- `finishSession()`
- `loadResult(sessionId)`
- `resetSession()`

Persist bằng `sessionStorage` key: `aiTutorSession`.

## 6. Backend Scope

Backend cần thêm module Tutor:

- `TutorController`
- `TutorService`
- DTO request/response
- Repository cho `ConversationSession` và `LearningLog`

Endpoints:

- `POST /api/v1/tutor/sessions`
- `GET /api/v1/tutor/sessions/{sessionId}`
- `POST /api/v1/tutor/sessions/{sessionId}/messages`
- `PATCH /api/v1/tutor/sessions/{sessionId}/finish`
- `GET /api/v1/tutor/sessions/{sessionId}/result`

Yêu cầu:

- Mọi endpoint yêu cầu JWT.
- Chỉ owner của session được đọc/ghi session.
- Lưu `conversation_sessions` khi tạo phiên.
- Lưu mỗi lượt chat vào `learning_logs`.
- `target_words` lưu JSONB trong `conversation_sessions.target_words`.
- `ended_at` cập nhật khi finish.

## 7. AI Layer Scope

AI layer xử lý:

- Nhận scenario, level, target words, conversation history.
- Nếu có audio:
  - STT chuyển audio sang transcript.
  - Pronunciation scoring nếu provider hỗ trợ.
- Chuẩn bị learning context từ scenario, level, target words và conversation history.
- LLM sinh phản hồi tutor:
  - thân thiện
  - đúng level
  - giữ hội thoại tự nhiên
  - sửa lỗi ngắn gọn, không làm người học mất nhịp nói
- TTS sinh audio phản hồi.

LangGraph có thể tách node:

```text
[START]
  -> load_session_context
  -> transcribe_audio_optional
  -> analyze_pronunciation_optional
  -> build_learning_context
  -> build_tutor_prompt
  -> generate_tutor_reply
  -> extract_corrections
  -> synthesize_voice_optional
  -> format_response
  -> [END]
```

## 8. Data Model Mapping

Sử dụng schema hiện có:

- `conversation_sessions`
  - `id`
  - `user_id`
  - `scenario_name`
  - `target_words`
  - `started_at`
  - `ended_at`
- `learning_logs`
  - `id`
  - `session_id`
  - `role`
  - `content`
  - `created_at`

Learning context của AI Tutor lấy từ:

- `conversation_sessions.scenario_name`
- `conversation_sessions.target_words`
- các dòng `learning_logs` trong session hiện tại
- thông tin level/thời lượng nhận từ request tạo session hoặc metadata session

Nếu cần lưu thêm voice analysis chi tiết, ưu tiên thêm bảng mới sau:

```text
tutor_feedback
- id
- session_id
- log_id
- feedback_type
- original_text
- corrected_text
- explanation
- score
- metadata jsonb
- created_at
```

V1 có thể chưa cần bảng này nếu result được AI tổng hợp lại từ logs.

## 9. Cập nhật API contract (audio, multipart, và streaming)

Đã cập nhật backend để hỗ trợ upload audio và streaming audio trả về. Tổng quan thay đổi API:

- Send message (voice): chấp nhận `multipart/form-data` với hai phần:
  - `metadata`: JSON string chứa `content` (nếu transcript client có), `inputMode`, `audioMetadata` (durationMs, mimeType)
  - `audio`: audio blob (file)

- Assistant audio streaming: backend lưu file audio vào `uploads/tutor-audio/{sessionId}/{filename}` và cung cấp endpoint:
  - `GET /api/v1/tutor/audio/{sessionId}/{filename}` để tải/stream file

- `Send message` response bổ sung trường `audioUrl` trên `message`:
  - `audioUrl` chứa đường dẫn tương đối: `/api/v1/tutor/audio/{sessionId}/{filename}`

- Nếu client gửi audio nhưng không gửi transcript, backend sẽ cố gắng gọi STT provider (hiện là mock). Nếu STT rỗng, backend vẫn lưu audio và tiếp tục xử lý bằng `request.content` (nếu có) hoặc trả lỗi nhẹ.

- TTS: backend có adapter TTS (mock) tạo file placeholder và lưu vào cùng thư mục session; `message.audioUrl` sẽ trỏ tới file TTS đó.

Ví dụ FormData gửi từ frontend (JS):

```js
const metadata = {
  content: "", // optional transcript
  inputMode: "voice",
  audioMetadata: { durationMs: 3200, mimeType: "audio/webm" },
};
const fd = new FormData();
fd.append("metadata", JSON.stringify(metadata));
fd.append("audio", blob, "recording.webm");
await api.post(`/api/v1/tutor/sessions/${sessionId}/messages`, fd, {
  headers: { "Content-Type": "multipart/form-data" },
});
```

Gợi ý cho frontend: khi nhận `message.audioUrl`, xây dựng URL đầy đủ bằng cách prefix base API host (ví dụ `https://api.example.com` hoặc `window.location.origin`) trước khi gán cho `audio.src`.

### Lưu ý bảo mật & kích thước

- Giới hạn kích thước upload trên backend (multipart) nên đặt (ví dụ 10MB) để tránh abuse.
- Kiểm tra MIME type và extension khi lưu file.
- Cân nhắc xóa file audio cũ theo lifecycle của session (cron job hoặc khi session kết thúc + TTL).

## 9. Error Handling

Frontend:

- Không có microphone permission: fallback text input.
- Browser không hỗ trợ Web Speech API: gửi audio cho backend hoặc dùng text input.
- STT lỗi: cho retry audio hoặc nhập text.
- TTS lỗi: vẫn hiển thị text response.
- Message API lỗi: giữ user message ở trạng thái failed và cho retry.
- Session hết hạn/không tồn tại: redirect về setup với banner.

Backend:

- Validate session owner.
- Validate `scenarioName`, `level`, `durationMinutes`.
- Validate message không rỗng.
- Timeout AI layer trả lỗi thân thiện.
- Không để lỗi AI làm mất log user message.

## 10. Test Plan

Frontend:

- `npm run build`.
- Test setup:
  - load decks thành công
  - deck rỗng
  - API lỗi
  - chọn level/duration/scenario
- Test chat:
  - gửi text
  - gửi voice có transcript
  - permission microphone bị từ chối
  - retry message failed
  - reload trang vẫn giữ session
  - finish session chuyển đúng result
- Test responsive:
  - desktop
  - tablet
  - mobile
  - không bị overlap với sidebar/bottom nav

Backend:

- Unit test service tạo session.
- Unit test kiểm tra owner session.
- Integration test message endpoint.
- Integration test finish session.
- Test AI layer timeout/failure fallback.

AI Layer:

- Test prompt theo từng scenario.
- Test learning context được build đúng từ scenario, target words và conversation history.
- Test output JSON có đủ `message`, `corrections`, `suggestions`, `newVocabulary`.
- Test transcript rỗng hoặc câu quá ngắn.

## 11. Acceptance Criteria

- Người học có thể bắt đầu một phiên AI Tutor từ deck thật.
- Người học có thể nói bằng microphone hoặc nhập text fallback.
- AI trả lời theo đúng scenario và level.
- Mỗi lượt user/assistant được lưu vào `learning_logs`.
- Kết thúc phiên hiển thị kết quả có điểm, lỗi sai, gợi ý và từ vựng.
- Reload trong lúc chat không làm mất session hiện tại.
- UI có loading, empty, error states đầy đủ.

## 12. Assumptions

- Backend Tutor endpoints chưa có sẵn, cần implement sau hoặc mock tạm trong giai đoạn frontend.
- Voice STT/TTS provider sẽ được quyết định ở backend/AI layer.
- PostgreSQL là nguồn lưu session/log chính.
- AI Tutor voice không dùng GraphRAG/Neo4j; mọi ngữ cảnh học tập đến từ deck, target words, scenario và history của session.
