-- =============================================================================
-- MIGRATION SCRIPT: Schema cũ (app_users/flashcards cũ) → Schema mới
-- Tạo: 2026-04-15
--
-- CHẠY THEO THỨ TỰ từ trên xuống.
-- Backup DB trước khi chạy: pg_dump datn > datn_backup.sql
-- =============================================================================

-- ---------------------------------------------------------------------------
-- BƯỚC 1: Tạo các Enum types mới (nếu chưa có)
-- ---------------------------------------------------------------------------
DO $$ BEGIN
    CREATE TYPE role_enum AS ENUM ('ADMIN', 'USER');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE target_level_enum AS ENUM ('N5', 'N4', 'N3');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE flashcard_status AS ENUM ('learning', 'reviewing', 'mastered');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE entry_type_enum AS ENUM ('word', 'kanji', 'grammar');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;


-- ---------------------------------------------------------------------------
-- BƯỚC 2: Tạo bảng `users` mới (thay thế `app_users`)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(255) UNIQUE NOT NULL,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR      NOT NULL,
    role            role_enum    NOT NULL DEFAULT 'USER',
    target_level    target_level_enum     DEFAULT 'N5',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Migrate data từ app_users → users
-- username tạm thời lấy phần trước @ của email (hoặc dùng UUID nếu trùng)
INSERT INTO users (id, username, email, password_hash, role, created_at)
SELECT
    id,
    -- Tạo username từ phần trước @ của email, thêm số ngẫu nhiên nếu cần
    LOWER(SPLIT_PART(email, '@', 1)) || '_' || FLOOR(RANDOM() * 9000 + 1000)::TEXT,
    email,
    password,  -- cột password cũ đã là bcrypt hash
    CASE
        WHEN role::TEXT = 'ADMIN' THEN 'ADMIN'::role_enum
        ELSE 'USER'::role_enum
    END,
    created_at
FROM app_users
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------------------
-- BƯỚC 3: Tạo bảng `user_profiles`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id             UUID    PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    daily_goal_minutes  INT     NOT NULL DEFAULT 30,
    streak_count        INT     NOT NULL DEFAULT 0,
    total_points        INT     NOT NULL DEFAULT 0,
    last_active         TIMESTAMPTZ
);

-- Tạo profile mặc định cho tất cả users đã migrate
INSERT INTO user_profiles (user_id)
SELECT id FROM users
ON CONFLICT (user_id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- BƯỚC 4: Cập nhật bảng `dictionary_entries`
--   - Bỏ: part_of_speech, example, example_translation, updated_at
--   - Đổi kiểu entry_type và jlpt_level sang native enum
-- ---------------------------------------------------------------------------

-- 4a. Thêm cột enum mới nếu chưa có
ALTER TABLE dictionary_entries
    ADD COLUMN IF NOT EXISTS entry_type_new entry_type_enum,
    ADD COLUMN IF NOT EXISTS jlpt_level_new target_level_enum;

-- 4b. Copy data sang cột enum mới
UPDATE dictionary_entries SET
    entry_type_new = entry_type::entry_type_enum,
    jlpt_level_new = CASE
        WHEN jlpt_level IN ('N5','N4','N3') THEN jlpt_level::target_level_enum
        ELSE NULL
    END;

-- 4c. Drop cột cũ và đổi tên cột mới
ALTER TABLE dictionary_entries
    DROP COLUMN IF EXISTS entry_type,
    DROP COLUMN IF EXISTS jlpt_level,
    DROP COLUMN IF EXISTS part_of_speech,
    DROP COLUMN IF EXISTS example,
    DROP COLUMN IF EXISTS example_translation,
    DROP COLUMN IF EXISTS updated_at;

ALTER TABLE dictionary_entries
    RENAME COLUMN entry_type_new TO entry_type;
ALTER TABLE dictionary_entries
    RENAME COLUMN jlpt_level_new TO jlpt_level;

-- 4d. Đảm bảo created_at có default
ALTER TABLE dictionary_entries
    ALTER COLUMN created_at SET DEFAULT NOW();


-- ---------------------------------------------------------------------------
-- BƯỚC 5: Tạo bảng `entry_relations`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS entry_relations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id       UUID        NOT NULL REFERENCES dictionary_entries(id) ON DELETE CASCADE,
    target_id       UUID        NOT NULL REFERENCES dictionary_entries(id) ON DELETE CASCADE,
    relation_type   VARCHAR(50) DEFAULT 'kanji_component',
    order_index     INT
);

CREATE INDEX IF NOT EXISTS idx_entry_relations_source ON entry_relations(source_id);
CREATE INDEX IF NOT EXISTS idx_entry_relations_target ON entry_relations(target_id);


-- ---------------------------------------------------------------------------
-- BƯỚC 6: Tạo bảng `examples`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS examples (
    id                  UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id            UUID    NOT NULL REFERENCES dictionary_entries(id) ON DELETE CASCADE,
    japanese_sentence   TEXT    NOT NULL,
    vietnamese_sentence TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_examples_entry ON examples(entry_id);

-- NOTE: Data ví dụ cũ (cột example/example_translation trong dictionary_entries)
-- đã bị xóa ở Bước 4. Sẽ được import lại từ file JSON bằng import_to_postgres.py.


-- ---------------------------------------------------------------------------
-- BƯỚC 7: Tạo bảng `flashcard_decks`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS flashcard_decks (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_public   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_flashcard_decks_user ON flashcard_decks(user_id);


-- ---------------------------------------------------------------------------
-- BƯỚC 8: Tạo bảng `flashcards` mới
--   Schema cũ: (user_id, entry_id, source, status, repetitions, interval, ease_factor, next_review_at)
--   Schema mới: (deck_id, user_id, front_text, front_reading, back_text, back_notes, status)
--
--   ⚠️  Data cũ KHÔNG thể migrate tự động vì cấu trúc hoàn toàn khác.
--   Các flashcard cũ sẽ bị xóa. Users cần tạo lại từ đầu.
-- ---------------------------------------------------------------------------

-- Xóa bảng flashcards cũ (nếu tồn tại với schema cũ)
DROP TABLE IF EXISTS flashcards CASCADE;

CREATE TABLE flashcards (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_id         UUID            NOT NULL REFERENCES flashcard_decks(id) ON DELETE CASCADE,
    user_id         UUID            NOT NULL REFERENCES users(id),
    front_text      TEXT            NOT NULL,
    front_reading   TEXT,
    back_text       TEXT            NOT NULL,
    back_notes      TEXT,
    status          flashcard_status NOT NULL DEFAULT 'learning',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_flashcards_deck   ON flashcards(deck_id);
CREATE INDEX IF NOT EXISTS idx_flashcards_user   ON flashcards(user_id);
CREATE INDEX IF NOT EXISTS idx_flashcards_status ON flashcards(status);


-- ---------------------------------------------------------------------------
-- BƯỚC 9: Tạo bảng `srs_details`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS srs_details (
    flashcard_id    UUID    PRIMARY KEY REFERENCES flashcards(id) ON DELETE CASCADE,
    ease_factor     FLOAT   NOT NULL DEFAULT 2.5,
    interval_days   INT     NOT NULL DEFAULT 0,
    repetitions     INT     NOT NULL DEFAULT 0,
    next_review     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ---------------------------------------------------------------------------
-- BƯỚC 10: Tạo bảng `conversation_sessions`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversation_sessions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id),
    scenario_name   VARCHAR(255),
    target_words    JSONB,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at        TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_conv_sessions_user ON conversation_sessions(user_id);


-- ---------------------------------------------------------------------------
-- BƯỚC 11: Tạo bảng `learning_logs`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS learning_logs (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  UUID    NOT NULL REFERENCES conversation_sessions(id) ON DELETE CASCADE,
    role        VARCHAR(50),
    content     TEXT    NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_learning_logs_session ON learning_logs(session_id);


-- ---------------------------------------------------------------------------
-- BƯỚC 12: Tạo bảng `comments`
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id    UUID    NOT NULL REFERENCES dictionary_entries(id),
    user_id     UUID    NOT NULL REFERENCES users(id),
    parent_id   UUID    REFERENCES comments(id),
    content     TEXT    NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_entry  ON comments(entry_id);
CREATE INDEX IF NOT EXISTS idx_comments_user   ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_id);


-- ---------------------------------------------------------------------------
-- BƯỚC 13: Cập nhật `app_refresh_tokens` — đổi FK trỏ sang bảng `users` mới
-- ---------------------------------------------------------------------------
-- Xóa FK cũ trỏ app_users (tên constraint có thể khác, dùng DO block để an toàn)
DO $$
DECLARE
    v_constraint TEXT;
BEGIN
    SELECT tc.constraint_name INTO v_constraint
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
        ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'app_refresh_tokens'
      AND tc.constraint_type = 'FOREIGN KEY'
      AND kcu.column_name = 'user_id'
    LIMIT 1;

    IF v_constraint IS NOT NULL THEN
        EXECUTE 'ALTER TABLE app_refresh_tokens DROP CONSTRAINT ' || quote_ident(v_constraint);
    END IF;
END $$;

-- Thêm FK mới trỏ sang users
ALTER TABLE app_refresh_tokens
    ADD CONSTRAINT fk_refresh_token_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


-- ---------------------------------------------------------------------------
-- BƯỚC 14 (Tùy chọn): Xóa bảng app_users sau khi verify migration xong
-- ---------------------------------------------------------------------------
-- COMMENT OUT nếu chưa muốn xóa ngay:
-- DROP TABLE IF EXISTS app_users CASCADE;


-- ---------------------------------------------------------------------------
-- KIỂM TRA CUỐI: Đếm số bản ghi đã migrate
-- ---------------------------------------------------------------------------
SELECT 'users'              AS tbl, COUNT(*) FROM users
UNION ALL
SELECT 'user_profiles',              COUNT(*) FROM user_profiles
UNION ALL
SELECT 'dictionary_entries',         COUNT(*) FROM dictionary_entries
UNION ALL
SELECT 'entry_relations',            COUNT(*) FROM entry_relations
UNION ALL
SELECT 'examples',                   COUNT(*) FROM examples
UNION ALL
SELECT 'flashcard_decks',            COUNT(*) FROM flashcard_decks
UNION ALL
SELECT 'flashcards',                 COUNT(*) FROM flashcards
UNION ALL
SELECT 'srs_details',                COUNT(*) FROM srs_details
UNION ALL
SELECT 'conversation_sessions',      COUNT(*) FROM conversation_sessions
UNION ALL
SELECT 'learning_logs',              COUNT(*) FROM learning_logs
UNION ALL
SELECT 'comments',                   COUNT(*) FROM comments
ORDER BY 1;
