# DATN — Smart Japanese Learning System: Project Summary

**Cập nhật lần cuối:** 2026-05-12 (09:00)

**Scope:** Backend hoàn thành (auth + Dictionary + Comment APIs) · Frontend Dictionary/Kanji/Grammar hoàn thiện UI + search + comment integration

**Lưu ý:** Thư mục chứa tài liệu dự án là `docs`.

---

## Recent updates (2026-05-12)

- Schema normalization: removed `user_id` from `flashcards` and moved ownership to `flashcard_decks.user_id`.
- Fixed `findDueByDeckAndUser` repository query so both LEARNING and REVIEW statuses respect `srs_details.next_review`.
- Implemented SM-2 dynamic interval calculation in `StudySessionView` (buttons now show per-card intervals).
- Added "Thêm vào Flashcard" modal and in-modal deck creation to `DictionaryView.vue`, `GrammarView.vue`, and `KanjiView.vue`.
- Updated modal UI to follow design.md (glassmorphism backdrop, ghost borders, ambient shadows, larger radii, gradient CTA buttons).
- Frontend `npm run build` completed successfully after fixes; backend compilation also validated.


Email : test2@gmail.com
Password : 12345678

## 0. Tổng quan kiến trúc hệ thống

```
┌──────────────────────────────────────────────────────────────┐
│                     CLIENT (Browser)                        │
│              Vue 3 + Tailwind CSS + Pinia                   │
│                     localhost:5173                          │
└────────────────────────┬────────────────────────────────────┘
                         │ REST API (HTTP/JSON)
                         │ proxy /api, /auth → :8080
┌────────────────────────▼────────────────────────────────────┐
│                 BACKEND (Spring Boot 4)                     │
│          JWT Auth · CRUD · Service-Repository               │
│                     localhost:8080                          │
└───────────┬───────────────────────────────┬─────────────────┘
            │ JDBC/JPA                      │ gRPC + Protobuf 
            │                               │ / rest api
┌───────────▼────────────┐        ┌─────────▼────────────────────┐
│      PostgreSQL         │        │      AI Layer (Python)       │
│      datn DB            │        │   LangGraph StateGraph       │
│      :5432              │        │   Multi-agent + GraphRAG     │
└─────────────────────────┘        └──────────┬──────────────────┘
                                               │
                                               │ Neo4j Driver
                                      ┌────────▼─────────┐
                                      │      Neo4j       │
                                      │      :7687       │
                                      └──────────────────┘
```

**Core Logic:** SRS (Spaced Repetition) — thuật toán SM-2

---

## 1. Tech Stack

| Layer            | Technology                                                          |
| ---------------- | ------------------------------------------------------------------- |
| Backend          | Java 21, Spring Boot 4.0.5                                          |
| Security         | Spring Security 6.4+, JJWT 0.12.6                                   |
| Frontend         | Vue 3, Vite 8, Tailwind CSS v4                                      |
| State Management | Pinia                                                               |
| HTTP Client      | Axios (với JWT interceptors)                                        |
| Database         | PostgreSQL (`datn` DB)                                              |
| ORM              | JPA / Hibernate 7                                                   |
| AI Layer         | Python, LangGraph StateGraph                                        |
| Communication    | REST (Frontend↔Backend), gRPC/Protobuf (Backend↔AI)                 |
| Utilities        | Lombok, Bean Validation, @vueuse/core                               |
| JDK (runtime)    | JetBrains JDK 25 (bundled IntelliJ) — `JAVA_HOME` phải set thủ công |

---

## 2. Coding Style & Conventions

- **Nguyên tắc chung:** Clean Code, SOLID principles.
- **Frontend:** Composition API, `<script setup>`, Pinia cho state toàn cục.
- **Backend:** RESTful API, Service-Repository pattern.
- **AI:** Multi-agent architecture dùng LangGraph StateGraph.

### Standard Response Envelope (tất cả API phải dùng)

```json
{
  "status": "success | error",
  "code": 200,
  "message": "Thông báo ngắn gọn",
  "data": {},
  "timestamp": "2026-04-12T15:00:00Z"
}
```

- Khi lỗi: `status = "error"`, `code` = HTTP status, `data = null` hoặc `{}`.

### gRPC (Backend ↔ AI Service)

- Dùng Protobuf để định nghĩa Service và Message.
- Message phải rõ ràng, có validation (`required` fields).
- Response proto luôn có `status` (OK/ERROR) và `message`.

---

## 3. Backend — Package Structure

```
vn.hust.huy.backend/
├── model/
│   ├── entity/
│   │   ├── User.java                  → bảng: users
│   │   ├── UserProfile.java           → bảng: user_profiles (1-1 users)
│   │   ├── RefreshToken.java          → bảng: app_refresh_tokens (auth only)
│   │   ├── DictionaryEntry.java       → bảng: dictionary_entries
│   │   ├── EntryRelation.java         → bảng: entry_relations (Word-Kanji)
│   │   ├── Example.java               → bảng: examples
│   │   ├── FlashcardDeck.java         → bảng: flashcard_decks
│   │   ├── Flashcard.java             → bảng: flashcards
│   │   ├── SrsDetail.java             → bảng: srs_details (1-1 flashcards)
│   │   ├── ConversationSession.java   → bảng: conversation_sessions
│   │   ├── LearningLog.java           → bảng: learning_logs
│   │   └── Comment.java               → bảng: comments
│   └── enums/
│       ├── Role.java            (ADMIN, USER)
│       ├── JlptLevel.java       (N5, N4, N3, N2, N1)
│       ├── EntryType.java       (word, kanji, grammar)
│       ├── FlashcardStatus.java (NEW_CARD→"new", LEARNING, REVIEW) ← @JsonValue + AttributeConverter
│       └── RelationType.java    (kanji, radical, compound, synonym)  ← MỚI
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java   (username, email, password, role, targetLevel)
│   │   ├── LoginRequest.java
│   │   ├── RefreshRequest.java
│   │   ├── DictionaryRequest.java (text, entryType, reading, meaningVn, jlptLevel, explanationShort)
│   │   └── FlashcardRequest.java  (deckId, frontText, frontReading, backText, backNotes)
│   └── response/
│       ├── ApiResponse.java       (status, code, message, data, timestamp)
│       ├── AuthResponse.java      (accessToken, refreshToken, tokenType)
│       ├── UserResponse.java      (id, username, email, role, targetLevel, createdAt)
│       ├── DictionaryResponse.java
│       └── FlashcardResponse.java (deckId, deckName, frontText, backText, status...)
├── repository/
│   ├── UserRepository.java              (findByEmail, existsByEmail, existsByUsername)
│   ├── RefreshTokenRepository.java      (findByToken, deleteByUser)
│   ├── DictionaryEntryRepository.java   (searchAll, searchByType, findByEntryType, findAll)
│   │                                    ↑ tách 2 query riêng — tránh JPQL null-binding với NAMED_ENUM
│   ├── FlashcardRepository.java         (findAllByUserWithDeck, findAllByDeckIdAndUser, findByIdAndUser)
│   ├── FlashcardDeckRepository.java     (findAllByUserOrderByCreatedAtDesc, findByIdAndUser)
│   └── CommentRepository.java           (findTopLevelByEntry, findRepliesByParentId)
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java    (dùng passwordHash)
│   └── SecurityConfig.java
├── service/
│   ├── AuthService + AuthServiceImpl          (register, login, refresh, logout)
│   ├── UserService + UserServiceImpl          (getCurrentUser)
│   ├── DictionaryService + DictionaryServiceImpl    (search, create, update)
│   ├── FlashcardService + FlashcardServiceImpl      (getMyFlashcards, getByDeck, create, delete)
│   ├── FlashcardDeckService + FlashcardDeckServiceImpl (getMyDecks, create, update, delete)
│   └── CommentService + CommentServiceImpl          (getCommentsByEntry, getReplies, create, delete)
├── controller/
│   ├── AuthController.java           (/auth/**)
│   ├── UserController.java           (/users/me)
│   ├── DictionaryController.java     (/api/v1/dictionary/**)
│   ├── FlashcardController.java      (/api/v1/flashcards/**)
│   ├── FlashcardDeckController.java  (/api/v1/decks/**)
│   └── CommentController.java        (/api/v1/comments/**)
└── exception/
    ├── ErrorCode.java
    ├── AppException.java
    └── GlobalExceptionHandler.java
```

---

## 4. Frontend — Cấu trúc Vue 3

```
frontend/
├── index.html                     ← Entry HTML (Google Fonts, SEO meta)
├── vite.config.js                 ← Tailwind plugin, @ alias, dev proxy
└── src/
    ├── main.js                    ← Bootstrap: Vue + Pinia + Router
    ├── App.vue                    ← Root component (chỉ <RouterView />)
    ├── assets/
    │   └── main.css               ← Design system: glassmorphism, buttons, inputs
    ├── services/
    │   ├── api.js                 ← Axios instance + JWT interceptors + auto-refresh
    │   ├── dictionary.js          ← search(q, type, page, size) + getById(id)
    │   └── comment.js             ← getByEntry, getReplies, create, delete  ← MỚI
    │   └── flashcard.js          ← client helpers for adding entries to decks (modal integration)
    ├── stores/
    │   └── auth.js                ← Pinia: login/logout/refresh + localStorage persist
    ├── router/
    │   └── index.js               ← Routes + navigation guards (auth/guest)
    ├── components/
    │   ├── layout/
    │   │   └── AppLayout.vue      ← Sidebar layout (nav + user info + logout)
    │   └── dictionary/
    │       ├── SearchHeader.vue   ← Debounced search bar + suggestion chips
    │       └── CommentSection.vue ← Threaded comments: load/post/reply/delete  ← MỚI
    ├── views/
    │   ├── auth/
    │   │   ├── LoginView.vue      ← Form đăng nhập + redirect sau login
    │   │   └── RegisterView.vue   ← Form đăng ký + JLPT level picker
    │   ├── dictionary/
    │   │   ├── DictionaryView.vue       ← Debounced search + type filter + JLPT badge
    │   │   └── DictionaryDetailView.vue ← Chi tiết 1 từ
    │   ├── flashcard/
    │   │   ├── FlashcardsView.vue ← Deck grid + create modal + delete
    │   │   └── StudyView.vue      ← 3D flip card + progress bar + done state
    │   │   └── StudySessionView.vue ← SM-2 dynamic interval calculation (buttons show per-card intervals)
    │   └── profile/
    │       └── ProfileView.vue    ← Hiển thị user info, level, role
    │   ├── review/
    │   │   ├── ReviewSetupView.vue    ← Màn hình chọn chế độ ôn tập
    │   │   ├── ReviewQuizView.vue     ← Trắc nghiệm điền từ
    │   │   └── ReviewStoryView.vue    ← Câu chuyện tương tác
    │   ├── tutor/
    │   │   ├── TutorSetupView.vue     ← Thiết lập phiên gia sư AI
    │   │   ├── TutorChatView.vue      ← Hội thoại tương tác AI
    │   │   └── TutorResultView.vue    ← Kết quả phiên học AI
    ├── composables/               ← (trống — dành cho custom composables sau)
    ├── utils/                     ← (trống — dành cho helper functions sau)
    └── types/                     ← (trống — dành cho TypeScript types sau)
```

### Luồng request từ Frontend

```
Vue Component
  → api.js (Axios)              ← tự gắn Bearer token
  → Vite Dev Proxy              ← /api, /auth → localhost:8080
  → Spring Boot Controller
  → Service → Repository → PostgreSQL
```

### Cấu hình proxy (vite.config.js)

```js
server: {
  port: 5173,
  proxy: {
    '/api':  { target: 'http://localhost:8080', changeOrigin: true },
    '/auth': { target: 'http://localhost:8080', changeOrigin: true },
  }
}
```

### JWT Auto-Refresh Flow (api.js)

```
Request → 401 response
  → isRefreshing = true
  → POST /auth/refresh
  → update accessToken trong store + localStorage
  → retry original request
  → các request đang pending được xử lý từ failedQueue
```

---

## 5. Kiến trúc JWT Authentication

### Flow tổng quan

```
Request
  → JwtAuthenticationFilter   (trích xuất Bearer token, validate, set SecurityContext)
  → SecurityConfig            (stateless, /auth/** public, else authenticated)
  → Controller                (@PreAuthorize RBAC)
  → Service                   (business logic)
  → Repository                (JPA)
  → PostgreSQL
```

### JWT Payload

```json
{
  "sub": "user@hust.vn",
  "userId": "550e8400-...",
  "role": "USER | ADMIN",
  "iat": 1234567890,
  "exp": 1234568790
}
```

### Token lifecycle

| Token         | Thời hạn | Lưu ở đâu                                       | Mục đích             |
| ------------- | -------- | ----------------------------------------------- | -------------------- |
| Access Token  | 15 phút  | Client only (memory + localStorage)             | Gọi API              |
| Refresh Token | 7 ngày   | Client localStorage + DB (`app_refresh_tokens`) | Lấy access token mới |

---

## 6. API Endpoints

| Method | Path                             | Auth      | Mô tả                                               |
| ------ | -------------------------------- | --------- | --------------------------------------------------- |
| POST   | `/auth/register`                 | ❌ Public | Đăng ký (username + email + password + targetLevel) |
| POST   | `/auth/login`                    | ❌ Public | Đăng nhập → `accessToken` + `refreshToken`          |
| POST   | `/auth/refresh`                  | ❌ Public | Đổi refresh token → access token mới                |
| POST   | `/auth/logout`                   | ❌ Public | Xóa refresh token khỏi DB                           |
| GET    | `/users/me`                      | ✅ JWT    | Lấy profile user hiện tại                           |
| GET    | `/api/v1/dictionary`             | ✅ JWT    | Tìm kiếm từ điển (`?q=...&type=word\|kanji\|grammar&page=0&size=N`) |
| GET    | `/api/v1/dictionary/{id}`        | ✅ JWT    | Chi tiết 1 entry kèm relations + examples                          |
| POST   | `/api/v1/dictionary`             | ✅ ADMIN  | Thêm từ mới                                                        |
| PUT    | `/api/v1/dictionary/{id}`        | ✅ ADMIN  | Cập nhật từ                                                        |
| GET    | `/api/v1/decks`                  | ✅ JWT    | Danh sách bộ thẻ của user                           |
| POST   | `/api/v1/decks`                  | ✅ JWT    | Tạo bộ thẻ mới                                      |
| PUT    | `/api/v1/decks/{id}`             | ✅ JWT    | Cập nhật bộ thẻ                                     |
| DELETE | `/api/v1/decks/{id}`             | ✅ JWT    | Xóa bộ thẻ (cascade flashcards)                     |
| GET    | `/api/v1/flashcards`             | ✅ JWT    | Tất cả flashcard của user                           |
| GET    | `/api/v1/flashcards?deckId={id}` | ✅ JWT    | Flashcard theo bộ thẻ                               |
| GET    | `/api/v1/flashcards/{id}`        | ✅ JWT    | Chi tiết 1 flashcard kèm `srsInfo`                  |
| POST   | `/api/v1/flashcards`             | ✅ JWT    | Tạo flashcard trong deck (auto-create SRS state)    |
| PUT    | `/api/v1/flashcards/{id}`        | ✅ JWT    | Cập nhật nội dung flashcard (front/back/notes)      |
| PATCH  | `/api/v1/flashcards/{id}/review` | ✅ JWT    | Submit kết quả ôn tập SM-2 (`rating`: again/hard/good/easy) |
| DELETE | `/api/v1/flashcards/{id}`        | ✅ JWT    | Xóa flashcard                                       |
| GET    | `/api/v1/comments?entryId={id}`  | ✅ JWT    | Top-level comments của 1 từ                         |
| GET    | `/api/v1/comments/{id}/replies`  | ✅ JWT    | Replies của 1 comment                               |
| POST   | `/api/v1/comments`               | ✅ JWT    | Đăng comment / reply                                |
| DELETE | `/api/v1/comments/{id}`          | ✅ JWT    | Xóa comment (chỉ tác giả hoặc ADMIN)                |

---

## 7. Các thay đổi schema lớn (2026-04-15)

### Bảng thay đổi

| Bảng cũ              | Bảng mới             | Thay đổi                                                            |
| -------------------- | -------------------- | ------------------------------------------------------------------- |
| `app_users`          | `users`              | Thêm `username`, `target_level`; `password` → `password_hash`       |
| `flashcards`         | `flashcards`         | Redesign: bỏ `entry_id`, thêm `deck_id`, `front_text`, `back_text`  |
| `dictionary_entries` | `dictionary_entries` | Bỏ `part_of_speech`, `example`, `example_translation`, `updated_at` |

### Bảng mới

| Bảng                    | Mô tả                                        |
| ----------------------- | -------------------------------------------- |
| `user_profiles`         | Cấu hình cá nhân: daily goal, streak, points |
| `entry_relations`       | Liên kết Word-Kanji-component (N:N self-ref) |
| `examples`              | Câu ví dụ tách riêng khỏi dictionary_entries |
| `flashcard_decks`       | Bộ thẻ (collection) của user                 |
| `srs_details`           | SRS/SM-2 fields tách riêng khỏi flashcards   |
| `conversation_sessions` | Phiên hội thoại AI của user                  |
| `learning_logs`         | Log từng tin nhắn trong conversation         |
| `comments`              | Comment (có thread) trên dictionary entry    |

---

## 8. Các vấn đề phát sinh & cách giải quyết

### Vấn đề 1: `JAVA_HOME` chưa set

- **Fix:** Dùng JetBrains JDK bundled với IntelliJ IDEA

```bash
export JAVA_HOME="/home/huutran/.local/share/JetBrains/Toolbox/apps/intellij-idea/jbr"
./mvnw spring-boot:run
```

### Vấn đề 2: `DaoAuthenticationProvider` compilation error (Spring Boot 4.x)

```java
// ✅ Spring Boot 4.x
DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
```

### Vấn đề 3: Bảng cũ xung đột schema

- **Fix:** Đổi tên bảng auth sang `app_users`, `app_refresh_tokens` để tách biệt domain schema.

### Vấn đề 4: `pg_dump` lỗi "role not found"

- **Nguyên nhân:** Default user là OS user `huutran`, PostgreSQL không có role này.
- **Fix:** Thêm `-U postgres -h localhost` vào lệnh

```bash
pg_dump -U postgres -h localhost datn > backup.sql
```

### Vấn đề 5: INSERT users fail — `null value in column "password"`

- **Nguyên nhân:** Hibernate `ddl-auto: update` tạo bảng `users` với cả cột `password` (cũ) lẫn `password_hash` (mới).
- **Fix:** `ALTER TABLE users DROP COLUMN IF EXISTS password;`

### Vấn đề 6: App hang tại Hibernate init (không khởi động được)

- **Nguyên nhân:** Cột `role` trong `users` vẫn là `varchar(20)` nhưng entity dùng `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` trỏ tới `role_enum`. Hibernate cố alter column type → block.
- **Fix:**

```sql
ALTER TABLE users
  ALTER COLUMN role TYPE role_enum
  USING role::TEXT::role_enum;
```

### Vấn đề 7: Tailwind v4 `@apply` với custom class trong cùng file

- **Nguyên nhân:** `@layer components` không thể `@apply` class tự định nghĩa trong cùng file (khác v3).
- **Fix:** Viết component class bằng CSS thuần thay vì `@apply`.

---

## 9. Các quyết định thiết kế quan trọng

| Quyết định                         | Lý do                                                                |
| ---------------------------------- | -------------------------------------------------------------------- |
| Flashcard tách khỏi dictionary     | User muốn tự biên soạn nội dung thẻ, không bị ràng buộc vào từ điển  |
| SRS fields tách ra `srs_details`   | Single Responsibility — dữ liệu thuật toán tách khỏi dữ liệu thẻ     |
| `examples` tách bảng riêng         | Một từ có nhiều câu ví dụ (1:N) — chuẩn hóa DB                       |
| `entry_relations` self-referencing | Linh hoạt: hỗ trợ cả kanji_component, synonym, antonym cùng 1 bảng   |
| `password` → `password_hash`       | Tên rõ ràng hơn, tránh nhầm lẫn với raw password                     |
| `app_refresh_tokens` giữ nguyên    | Không phải domain entity — tách biệt để dễ swap sang Redis sau       |
| Logout dùng refresh token          | Access token stateless — không thể thu hồi, tự hết sau 15 phút       |
| 1 refresh token/user               | Enforce single-session, ngăn token accumulation                      |
| Vite proxy `/api`, `/auth`         | Tránh CORS trong dev, không cần cấu hình backend                     |
| `entry_relations.relationType` là `String` | Tránh Hibernate throw khi DB có giá trị `antonym` không có trong enum `RelationType` |
| `DictionaryEntryRepository` tách 2 methods | JPQL `(:type IS NULL OR d.entryType = :type)` không hoạt động với PostgreSQL NAMED_ENUM khi type=null |
| Không auto-search khi mount              | Cho phép hiển thị empty state truyền cảm hứng trước khi user nhập từ |
| Suggestion chips trong empty state       | Click chip → tự động trigger search, giảm friction ban đầu |

---

## 10. Cấu hình

### Backend (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/datn
    username: postgres
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update

app:
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
    access-token-expiration: 900000 # 15 phút
    refresh-token-expiration: 604800000 # 7 ngày
```

### Frontend (Node.js — cài qua nvm)

```bash
# Node version đang dùng
node -v   # v24.15.0
npm -v    # v11.12.1
```

---

## 11. Lệnh chạy

### Backend
- [x] Schema normalization: removed `user_id` from `flashcards` and moved ownership to `flashcard_decks.user_id` (migration applied)
- [x] Fixed `findDueByDeckAndUser` repository query so both LEARNING and REVIEW statuses respect `srs_details.next_review`
```bash
export JAVA_HOME="/home/huutran/.local/share/JetBrains/Toolbox/apps/intellij-idea/jbr"
cd "/home/huutran/Documents/hoc tap/DATN/backend"
./mvnw spring-boot:run
```

### Frontend

```bash
# Load nvm (nếu terminal mới)
export NVM_DIR="$HOME/.nvm" && [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

cd "/home/huutran/Documents/hoc tap/DATN/frontend"
npm run dev        # Dev server: http://localhost:5173
npm run build      # Production build → dist/
```

> **Lưu ý:** Backend phải chạy trước ở port 8080 để Vite proxy hoạt động.

---

## 12. Migration DB (2026-04-15)

### File migration

`/home/huutran/Documents/hoc tap/DATN/crawl-data/migrate_schema.sql`

### Trạng thái DB sau migration

| Bảng                 | Rows |
| -------------------- | ---- |
| `users`              | 3    |
| `user_profiles`      | 3    |
| `dictionary_entries` | 300  |
| `entry_relations`    | 312  |
| `examples`           | 78   |

### Cách chạy migration (khi cần reset)

```bash
# Backup
PGPASSWORD=123456 pg_dump -U postgres -h localhost datn > datn_backup_$(date +%Y%m%d).sql

# Chạy migration
PGPASSWORD=123456 psql -U postgres -h localhost -d datn -f crawl-data/migrate_schema.sql

# Fix enum type (nếu cần)
PGPASSWORD=123456 psql -U postgres -h localhost -d datn -c \
  "ALTER TABLE users ALTER COLUMN role TYPE role_enum USING role::TEXT::role_enum;"

# Re-import data từ JSON
cd crawl-data && python import_to_postgres.py --data-dir ./sample-data
```

> **Lưu ý:** Data crawl **không cần crawl lại** — `import_to_postgres.py` đã được viết cho schema mới.

---

## 13. Việc cần làm tiếp theo

### Backend

- [x] Implement CRUD cho `flashcard_decks`
- [x] Implement `comments` API (top-level + thread replies)
- [x] Implement `flashcards` API (by user + by deck)
- [x] Implement `GET /api/v1/flashcards/{id}` — chi tiết + `srsInfo`
- [x] Implement `PUT /api/v1/flashcards/{id}` — cập nhật nội dung thẻ
- [x] Implement `PATCH /api/v1/flashcards/{id}/review` — SM-2 SRS
- [x] `SM2Algorithm.java` utility class (pure static, theo spec sm2-algorithm.md)
- [x] Auto-create `SrsDetail` khi tạo flashcard mới
- [x] Viết SQL migration script (`migrate_schema.sql`)
- [x] Fix DB enum types (role_enum, entry_type_enum, target_level_enum)
- [x] Re-import data từ JSON (300 entries, 312 relations, 78 examples)
- [x] Backend khởi động thành công, API hoạt động
- [ ] Import data crawl đầy đủ (ngoài sample-data)
- [ ] Implement `conversation_sessions` + `learning_logs` (AI bridge)
- [ ] Implement `user_profiles` API (streak, daily goal, points)
- [ ] (Optional) Thêm Redis blacklist cho access token revocation
- [ ] (Optional) Swagger/OpenAPI documentation
- [ ] Viết unit/integration tests

### Frontend

- [x] Khởi tạo project Vue 3 + Vite + Tailwind CSS v4
- [x] Cài Pinia, Vue Router 4, Axios, @vueuse/core
- [x] Thiết lập design system (glassmorphism, Meditative Canvas)
- [x] Cấu hình Vite proxy → backend :8080
- [x] Auth store (Pinia) + JWT interceptors (Axios)
- [x] Router với navigation guards (auth/guest)
- [x] Layout sidebar (AppLayout)
- [x] LoginView, RegisterView
- [x] DashboardView
- [x] **DictionaryView** — Bento grid layout theo stitch.html:
    - [x] Debounced search → hiển thị kết quả đầu tiên
    - [x] Word Card + JLPT badge + Thêm Flashcard button
    - [x] Kanji Breakdown (relationType: kanji | radical)
    - [x] Từ vựng liên quan (relationType: compound | synonym)
    - [x] Ảnh cảm hứng cố định + caption "Với ngôn ngữ..."
    - [x] Community comment section (UI)
    - [x] Empty state: Meditative Canvas với suggestion chips
- [x] **KanjiView** — Full-width, bỏ list trái:
    - [x] Big Kanji hero (9rem) + Flashcard button
    - [x] Bộ thủ (radical) + Ví dụ câu + Từ ghép + Đồng nghĩa
    - [x] Community comment section (UI)
    - [x] Empty state: Meditative Canvas với suggestion chips
- [x] **GrammarView** — Full-width, bỏ list trái:
    - [x] Core Info Card + JLPT badges theo màu level
    - [x] Thêm Flashcard button (floating, tooltip hover)
    - [x] Ví dụ minh họa (numbered) + Ngữ pháp tương đương
    - [x] Community comment section (UI)
    - [x] Empty state: Meditative Canvas với suggestion chips
- [x] ReviewSetupView, ReviewQuizView, ReviewStoryView
- [x] TutorSetupView, TutorChatView, TutorResultView
- [x] ProfileView
- [x] **CommentSection.vue** — component dùng chung, tích hợp thực vào cả 3 views:
    - [x] Load top-level comments theo `entryId` (watch, auto-reload khi đổi từ)
    - [x] Gửi comment (`Ctrl+Enter` hoặc nút Gửi)
    - [x] **Threaded replies** — load on-demand, collapse/expand
    - [x] Avatar của user (chữ cái đầu username)
    - [x] `timeAgo()` — "vừa xong", "3 phút trước"...
    - [x] Xóa comment (chỉ hiện cho tác giả / ADMIN)
    - [x] Loading skeleton + empty state + error banner
- [x] FlashcardsView (Quản lý bộ thẻ + CardStats)
- [ ] StudyView (3D flip card)
- [x] Kết nối thực tế backend cho Flashcard Deck APIs (CRUD + Stats)
- [ ] Kết nối thực tế backend cho Flashcard Review APIs (GET/PATCH review)
- [x] Kết nối thực tế backend cho Comment APIs
- [ ] Implement SM-2 review UI (Easy/Hard/Again buttons)
- [ ] Responsive mobile layout
- [ ] Toast notifications

- [x] Thêm "Thêm vào Flashcard" modal và tạo deck trong modal cho `DictionaryView.vue`, `GrammarView.vue`, `KanjiView.vue`
- [x] Modal UI cập nhật theo `design.md` (glassmorphism backdrop, ghost borders, ambient shadows, larger radii, gradient CTA buttons)
- [x] Implemented SM-2 dynamic interval calculation in `StudySessionView` (buttons show per-card intervals)
- [x] Frontend `npm run build` completed successfully; backend compilation validated

---

---

## 14. Các lỗi đã fix (Backend)

### Lỗi 500 khi search từ Nhật (ví dụ: 丁寧)
- **Nguyên nhân:** JPQL `(:type IS NULL OR d.entryType = :type)` không hoạt động với PostgreSQL NAMED_ENUM khi `type = null`. Hibernate không thể bind null cho parameter có type là native enum.
- **Fix:** Tách thành 2 query method — `searchAll(q, pageable)` và `searchByType(q, type, pageable)`.

### Lỗi relations biến mất trong DictionaryView
- **Nguyên nhân:** DB có giá trị `antonym` trong `entry_relations.relation_type`, nhưng `RelationType` enum không khai báo `antonym` → Hibernate throw `IllegalArgumentException` khi đọc relations → `getById` fail im lặng.
- **Fix:** Revert `relationType` field về `String` trong `EntryRelation` entity và `RelationDto`. `RelationType` enum chỉ dùng làm tài liệu tham chiếu.

### Fix: `findDueByDeckAndUser` repository query
- **Nguyên nhân:** Query không tính `srs_details.next_review` đúng cho cả trạng thái LEARNING và REVIEW.
- **Fix:** Sửa repository method `findDueByDeckAndUser` để so sánh `srs_details.next_review` cho cả LEARNING và REVIEW — vấn đề đã được validated trên môi trường dev.

### Build/Compile
- **Note:** Backend compilation validated; frontend `npm run build` completed successfully after modal/style fixes.

---

## 15. Design System (design.md)

**Philosophy:** "The Meditative Canvas" — bắt chước sự tĩnh lặng của zen garden, dojo truyền thống.

**Nguyên tắc chính:**
- **No-Line Rule:** Không dùng 1px solid border. Ranh giới định nghĩa bằng tonal shift (background) và negative space.
- **Ambient Shadows:** `box-shadow` với màu tinted `on-surface` ở 4–6% opacity, blur 20–40px.
- **Typography Scale:** Plus Jakarta Sans (headline) + Inter (body). Hero kanji `display-lg` (3.5rem+) tạo khoảnh khắc trang trọng.
- **Surface Hierarchy:** `surface` → `surface-container-low` → `surface-container-lowest` (white) = stacked layers như frosted glass.
- **Primary:** Zen Blue `#45617d`, **Secondary:** Sakura Wood `#7c5556`.
