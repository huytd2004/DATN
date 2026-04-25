# 🗄️ Database Schema Design

Tài liệu này mô tả cấu trúc cơ sở dữ liệu cho hệ thống học tiếng Nhật, bao gồm quản lý người dùng, từ điển, hệ thống Flashcard tự biên soạn (SRS), và nhật ký hội thoại AI.

---

## 1. 📋 Enumerations (Enums)

Các kiểu dữ liệu liệt kê được sử dụng để đảm bảo tính nhất quán của dữ liệu.

| Tên Enum              | Giá trị                             | Ghi chú                   |
| :-------------------- | :---------------------------------- | :------------------------ |
| **role_enum**         | `ADMIN`, `USER`                     | Phân quyền người dùng     |
| **target_level_enum** | `N5`, `N4`, `N3`                    | Cấp độ JLPT mục tiêu      |
| **flashcard_status**  | `learning`, `reviewing`, `mastered` | Trạng thái của thẻ học    |
| **entry_type_enum**   | `word`, `kanji`, `grammar`          | Loại mục từ trong từ điển |

---

## 2. 👤 User Management Module

Lưu trữ thông tin tài khoản và cấu hình cá nhân của người dùng.

### Table: `users`

| Column            | Type              | Constraints      | Default             |
| :---------------- | :---------------- | :--------------- | :------------------ |
| **id**            | uuid              | PK               | `gen_random_uuid()` |
| **username**      | varchar           | Unique, Not Null |                     |
| **email**         | varchar           | Unique, Not Null |                     |
| **password_hash** | varchar           | Not Null         |                     |
| **role**          | role_enum         |                  | `USER`              |
| **target_level**  | target_level_enum |                  | `N5`                |
| **created_at**    | timestamptz       |                  | `now()`             |

### Table: `user_profiles`

| Column                 | Type        | Constraints       | Default |
| :--------------------- | :---------- | :---------------- | :------ |
| **user_id**            | uuid        | PK, FK (users.id) |         |
| **daily_goal_minutes** | int         |                   | `30`    |
| **streak_count**       | int         |                   | `0`     |
| **total_points**       | int         |                   | `0`     |
| **last_active**        | timestamptz |                   |         |

---

## 📖 Dictionary Module

Hệ thống từ điển dùng để tra cứu và làm dữ liệu tham khảo cho người dùng.

### Table: `dictionary_entries`

| Column                | Type              | Constraints | Default             |
| :-------------------- | :---------------- | :---------- | :------------------ |
| **id**                | uuid              | PK          | `gen_random_uuid()` |
| **entry_type**        | entry_type_enum   | Not Null    |                     |
| **text**              | varchar           | Not Null    |                     |
| **reading**           | varchar           |             |                     |
| **meaning_vn**        | text              | Not Null    |                     |
| **jlpt_level**        | target_level_enum |             |                     |
| **explanation_short** | text              |             |                     |
| **created_at**        | timestamptz       |             | `now()`             |

### Table: `entry_relations` (Word-Kanji Mapping)

_Mục đích: Liên kết từ vựng với các chữ Kanji thành phần hoặc các từ đồng nghĩa._

| Column            | Type    | Constraints                | Note                             |
| :---------------- | :------ | :------------------------- | :------------------------------- |
| **id**            | uuid    | PK                         |                                  |
| **source_id**     | uuid    | FK (dictionary_entries.id) | ID của từ vựng (Word)            |
| **target_id**     | uuid    | FK (dictionary_entries.id) | ID của Kanji thành phần          |
| **relation_type** | varchar |                            | `kanji_component`, `synonym`,... |
| **order_index**   | int     |                            | Thứ tự chữ trong từ              |

### Table: `examples`

| Column                  | Type | Constraints                |
| :---------------------- | :--- | :------------------------- |
| **id**                  | uuid | PK                         |
| **entry_id**            | uuid | FK (dictionary_entries.id) |
| **japanese_sentence**   | text | Not Null                   |
| **vietnamese_sentence** | text | Not Null                   |

---

## 🗃️ Flashcard Module (Custom Content)

Người dùng có thể tạo các bộ thẻ (Decks) và tự biên soạn nội dung thẻ học.

### Table: `flashcard_decks`

| Column          | Type        | Constraints   | Note                      |
| :-------------- | :---------- | :------------ | :------------------------ |
| **id**          | uuid        | PK            |                           |
| **user_id**     | uuid        | FK (users.id) | Chủ sở hữu bộ thẻ         |
| **name**        | varchar     | Not Null      | Tên bộ thẻ (VD: IT Words) |
| **description** | text        |               |                           |
| **is_public**   | boolean     |               | Mặc định: `false`         |
| **created_at**  | timestamptz |               |                           |

### Table: `flashcards`

| Column            | Type             | Constraints             | Note                |
| :---------------- | :--------------- | :---------------------- | :------------------ |
| **id**            | uuid             | PK                      |                     |
| **deck_id**       | uuid             | FK (flashcard_decks.id) | Thuộc bộ thẻ nào    |
| **user_id**       | uuid             | FK (users.id)           |                     |
| **front_text**    | text             | Not Null                | Câu hỏi / Từ vựng   |
| **front_reading** | text             |                         | Cách đọc (Furigana) |
| **back_text**     | text             | Not Null                | Nghĩa / Câu trả lời |
| **back_notes**    | text             |                         | Ghi chú thêm        |
| **status**        | flashcard_status |                         | `learning`          |

### Table: `srs_details` (Thuật toán lặp lại ngắt quãng)

| Column            | Type        | Constraints            | Default |
| :---------------- | :---------- | :--------------------- | :------ |
| **flashcard_id**  | uuid        | PK, FK (flashcards.id) |         |
| **ease_factor**   | float       |                        | `2.5`   |
| **interval_days** | int         |                        | `0`     |
| **repetitions**   | int         |                        | `0`     |
| **next_review**   | timestamptz |                        | `now()` |

---

## 🤖 AI Conversation & Logs

Lưu trữ lịch sử hội thoại và các tương tác của người dùng.

### Table: `conversation_sessions`

| Column            | Type        | Constraints                    |
| :---------------- | :---------- | :----------------------------- |
| **id**            | uuid        | PK                             |
| **user_id**       | uuid        | FK (users.id)                  |
| **scenario_name** | varchar     | Tên tình huống giả lập         |
| **target_words**  | jsonb       | Danh sách UUID từ điển cần học |
| **started_at**    | timestamptz |                                |
| **ended_at**      | timestamptz |                                |

### Table: `learning_logs`

| Column         | Type    | Constraints                   |
| :------------- | :------ | :---------------------------- |
| **id**         | uuid    | PK                            |
| **session_id** | uuid    | FK (conversation_sessions.id) |
| **role**       | varchar | `user` hoặc `bot`             |
| **content**    | text    | Nội dung tin nhắn             |

### Table: `comments` (Cộng đồng)

| Column        | Type | Constraints                |
| :------------ | :--- | :------------------------- |
| **id**        | uuid | PK                         |
| **entry_id**  | uuid | FK (dictionary_entries.id) |
| **user_id**   | uuid | FK (users.id)              |
| **parent_id** | uuid | Self-reference (Reply)     |
| **content**   | text |                            |

---

## 🔗 Database Relationships Summary

1.  **Users ↔ Profiles**: Quan hệ 1:1 (`cascade delete`).
2.  **Decks ↔ Flashcards**: Quan hệ 1:N. Xóa bộ thẻ sẽ xóa toàn bộ thẻ bên trong.
3.  **Flashcards ↔ SRS**: Quan hệ 1:1. Lưu thông số thuật toán cho từng thẻ.
4.  **Dictionary ↔ Entry Relations**: Quan hệ N:N tự tham chiếu (Self-referencing) qua bảng trung gian để tạo cấu trúc Word - Kanji.
5.  **Dictionary ↔ Examples**: Quan hệ 1:N. Một từ có nhiều câu ví dụ.
6.  **Conversations**: Mỗi người dùng có nhiều Session; mỗi Session có nhiều Message (Logs).

---
