--
-- PostgreSQL database dump
--

\restrict hwtMw4rsjG2WGRgyiMglhubm9RNMohxK8tkY1ydvc8XKgh3pZdR49cxXCp19W1w

-- Dumped from database version 16.13 (Ubuntu 16.13-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.13 (Ubuntu 16.13-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: entry_type_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.entry_type_enum AS ENUM (
    'word',
    'kanji',
    'grammar'
);


ALTER TYPE public.entry_type_enum OWNER TO postgres;

--
-- Name: entrytype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.entrytype AS ENUM (
    'grammar',
    'kanji',
    'word'
);


ALTER TYPE public.entrytype OWNER TO postgres;

--
-- Name: flashcard_status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.flashcard_status AS ENUM (
    'learning',
    'reviewing',
    'mastered'
);


ALTER TYPE public.flashcard_status OWNER TO postgres;

--
-- Name: flashcardstatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.flashcardstatus AS ENUM (
    'learning',
    'mastered',
    'reviewing'
);


ALTER TYPE public.flashcardstatus OWNER TO postgres;

--
-- Name: jlptlevel; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.jlptlevel AS ENUM (
    'N3',
    'N4',
    'N5'
);


ALTER TYPE public.jlptlevel OWNER TO postgres;

--
-- Name: target_level_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.target_level_enum AS ENUM (
    'N5',
    'N4',
    'N3'
);


ALTER TYPE public.target_level_enum OWNER TO postgres;

--
-- Name: CAST (public.entrytype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.entrytype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.flashcardstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.flashcardstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.jlptlevel AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.jlptlevel AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.entrytype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.entrytype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.flashcardstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.flashcardstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.jlptlevel); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.jlptlevel) WITH INOUT AS IMPLICIT;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.app_refresh_tokens (
    id uuid NOT NULL,
    expiry_date timestamp(6) with time zone NOT NULL,
    token character varying(512) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.app_refresh_tokens OWNER TO postgres;

--
-- Name: app_users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.app_users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    CONSTRAINT app_users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.app_users OWNER TO postgres;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comments (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    entry_id uuid NOT NULL,
    user_id uuid NOT NULL,
    parent_id uuid,
    content text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.comments OWNER TO postgres;

--
-- Name: conversation_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conversation_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    scenario_name character varying,
    target_words jsonb,
    started_at timestamp with time zone DEFAULT now(),
    ended_at timestamp with time zone
);


ALTER TABLE public.conversation_sessions OWNER TO postgres;

--
-- Name: detected_errors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detected_errors (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    log_id uuid NOT NULL,
    error_type character varying,
    original_text text,
    correction text,
    explanation text
);


ALTER TABLE public.detected_errors OWNER TO postgres;

--
-- Name: dictionary_entries; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dictionary_entries (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    entry_type public.entry_type_enum NOT NULL,
    text character varying(255) NOT NULL,
    reading character varying(500),
    meaning_vn text NOT NULL,
    jlpt_level public.target_level_enum,
    explanation_short text,
    created_at timestamp with time zone DEFAULT now(),
    example text,
    example_translation text,
    part_of_speech character varying(100),
    updated_at timestamp(6) with time zone
);


ALTER TABLE public.dictionary_entries OWNER TO postgres;

--
-- Name: dictionary_entries_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dictionary_entries_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.dictionary_entries_seq OWNER TO postgres;

--
-- Name: entry_relations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entry_relations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    source_id uuid NOT NULL,
    target_id uuid NOT NULL,
    relation_type character varying NOT NULL
);


ALTER TABLE public.entry_relations OWNER TO postgres;

--
-- Name: examples; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.examples (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    entry_id uuid NOT NULL,
    japanese_sentence text NOT NULL,
    vietnamese_sentence text NOT NULL
);


ALTER TABLE public.examples OWNER TO postgres;

--
-- Name: flashcards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flashcards (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    entry_id uuid NOT NULL,
    source character varying(255),
    status public.flashcard_status DEFAULT 'learning'::public.flashcard_status,
    created_at timestamp with time zone DEFAULT now(),
    ease_factor double precision NOT NULL,
    "interval" integer NOT NULL,
    next_review_at timestamp(6) with time zone,
    repetitions integer NOT NULL
);


ALTER TABLE public.flashcards OWNER TO postgres;

--
-- Name: learning_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.learning_logs (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    session_id uuid NOT NULL,
    role character varying NOT NULL,
    content text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.learning_logs OWNER TO postgres;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    id uuid NOT NULL,
    expiry_date timestamp(6) with time zone NOT NULL,
    token character varying(512) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- Name: srs_details; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.srs_details (
    flashcard_id uuid NOT NULL,
    ease_factor double precision DEFAULT 2.5,
    interval_days integer DEFAULT 0,
    repetitions integer DEFAULT 0,
    next_review timestamp with time zone DEFAULT now()
);


ALTER TABLE public.srs_details OWNER TO postgres;

--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_profiles (
    user_id uuid NOT NULL,
    daily_goal_minutes integer DEFAULT 30,
    streak_count integer DEFAULT 0,
    total_points integer DEFAULT 0,
    last_active timestamp with time zone
);


ALTER TABLE public.user_profiles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    username character varying NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying NOT NULL,
    target_level public.target_level_enum DEFAULT 'N5'::public.target_level_enum,
    created_at timestamp with time zone DEFAULT now(),
    password character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: app_refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.app_refresh_tokens (id, expiry_date, token, user_id) FROM stdin;
ab6fafe3-8699-437f-a467-c5f7d9fcf132	2026-04-19 22:35:04.564453+07	54348dcf-abbc-4327-be28-c65e8b989cc4	c3d4742b-6875-40eb-94fd-54270f0559e8
cc0aaf12-67a8-445d-808b-4e4dd60213b4	2026-04-20 15:19:04.922629+07	4a7faa29-f2b7-48a3-8fee-3fd64638ff5f	84f35e92-1846-44d6-b26f-b8c6dff4f88f
\.


--
-- Data for Name: app_users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.app_users (id, created_at, email, password, role) FROM stdin;
c3d4742b-6875-40eb-94fd-54270f0559e8	2026-04-12 22:34:31.788661+07	user@hust.vn	$2a$10$2sJ2Pb6C6ux8MLXin3vCSu.RxWae/FpKFO3f79qSmtv0MsQKAxICu	USER
011f9ee6-66e8-4760-8908-7f826c625bce	2026-04-13 14:43:04.498592+07	test@hust.vn	$2a$10$JMxoASqyIYWfKfx8Sf3HG.dB8jc57.i5QValVz.ZbSp52VH4a4.G.	USER
84f35e92-1846-44d6-b26f-b8c6dff4f88f	2026-04-13 14:51:40.971687+07	testdict@hust.vn	$2a$10$Mx1cObPNRyuzW3tFlS8EHOKdZQUec1QiRs/R8WAivhRhGJ0wbtKG2	USER
\.


--
-- Data for Name: comments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.comments (id, entry_id, user_id, parent_id, content, created_at) FROM stdin;
\.


--
-- Data for Name: conversation_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.conversation_sessions (id, user_id, scenario_name, target_words, started_at, ended_at) FROM stdin;
\.


--
-- Data for Name: detected_errors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detected_errors (id, log_id, error_type, original_text, correction, explanation) FROM stdin;
\.


--
-- Data for Name: dictionary_entries; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dictionary_entries (id, entry_type, text, reading, meaning_vn, jlpt_level, explanation_short, created_at, example, example_translation, part_of_speech, updated_at) FROM stdin;
4b781e2f-06da-4af0-9ee1-db12231d67e5	grammar	～てください	\N	Hãy làm gì đó	N5	Dùng để đưa ra lời yêu cầu hoặc nhờ vả một cách lịch sự.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
15540844-6077-4fa5-a8dd-ad3d61d6386e	grammar	～てなさい	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
330fa901-e35f-46b9-9fab-9006a7a06b62	grammar	～てくださいませんか	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
48768c0f-626c-4562-9e75-d8886d1042d7	grammar	～たことがある	\N	Đã từng làm gì đó	N5	Diễn tả một trải nghiệm hoặc kinh nghiệm trong quá khứ.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
187c683c-8c8d-4c0a-9059-64cd4f8814bb	grammar	～なければならない	\N	Phải làm gì đó	N4	Diễn tả một nghĩa vụ hoặc sự bắt buộc phải thực hiện.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
38042282-4946-49ee-995b-0a08edd2734a	grammar	～なくてはいけない	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
06316ec8-de6f-4033-a388-c0e943c9bb13	grammar	～なきゃ	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
cc54c6e3-12b6-4923-87fa-711b21cc8019	grammar	～ことができる	\N	Có thể làm gì đó	N5	Diễn tả khả năng hoặc năng lực của bản thân hoặc sự cho phép.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
050dc7d6-d14c-4dd5-88b4-0771f79d31ac	grammar	Động từ thể khả năng (V-える/V-られる)	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
43ec397a-37a2-433c-935e-40495fe27e27	grammar	～ほうがいい	\N	Nên / Không nên	N4	Dùng để đưa ra lời khuyên cho đối phương.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
d250088b-8180-490b-b14a-4663cf6e10c0	grammar	～たらどうですか	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
bcbe9058-efdf-430b-960a-06b04e043201	grammar	～つもりだ	\N	Dự định làm gì	N4	Diễn tả một ý định hoặc kế hoạch của người nói.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
c605c2da-f07a-4da0-b43b-030e92d70836	grammar	～ようと思う	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
4c11c0ad-7bd5-4c8b-9b1b-e660baba0d52	grammar	～予定だ	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
6f2071da-85f7-4a3f-a0a0-0a09592fc172	grammar	～すぎる	\N	Quá mức ...	N4	Diễn tả một trạng thái vượt quá giới hạn cho phép, thường mang nghĩa tiêu cực.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
7b381c3a-96da-4388-8716-7f5b3311d854	grammar	とても	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
bfc7fceb-b63d-4de8-8bd1-05d4890b955b	grammar	あまりに	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
4b36d5e8-0617-41af-bb0b-51b85e7a15a4	grammar	～ながら	\N	Vừa ... vừa ...	N4	Diễn tả hai hành động được thực hiện cùng một lúc bởi một người.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
1f4e65df-2ade-4f66-9af4-5f70118c7f5c	grammar	～ついでに	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
12206495-83bd-4795-a1e6-137af1a256a5	grammar	～とともに	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
f5af8af6-b458-48c2-9289-89611f584314	grammar	～はずだ	\N	Chắc chắn là ...	N3	Diễn tả một dự đoán có cơ sở chắc chắn của người nói.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
7d1fdb96-39ab-405f-93a8-09f1bde61041	grammar	～に違いない	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
dc173ead-9c67-4580-af8a-c3ccb2c7d41b	grammar	～だろう	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
d028bc1f-cc5f-4b55-a526-abb4950e5547	grammar	～ようだ	\N	Dường như / Hình như	N3	Diễn tả một sự phán đoán dựa trên cảm giác hoặc thông tin quan sát được.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
87fd6264-0cda-48f3-80a1-1fbca1108c19	grammar	～みたいだ	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
6619874a-73a1-4125-ae08-882f2f5364c1	grammar	～そうだ	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
74b7a2ad-edb0-4b22-bc9e-8874a277e517	grammar	～ために	\N	Để / Vì (mục đích)	N4	Diễn tả mục đích của hành động hoặc nguyên nhân.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
fda4b62c-53ab-4284-b45b-e61e284542c6	grammar	～ように	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
40d42513-b5ed-45b5-9d2b-62aa752cfaf1	grammar	～のに	\N	Mặc dù ... vậy mà ...	N4	Diễn tả sự bất ngờ, thất vọng về một kết quả trái ngược với kỳ vọng.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
14ddbdc9-af90-4f77-be10-4feef161596f	grammar	～けれども	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
563d7219-c40b-4a16-9883-f42113b6e57f	grammar	～が	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
3ce52a73-530f-4218-a1bb-130869504339	grammar	～うちに	\N	Trong lúc còn ... (thì làm gì đó)	N3	Tranh thủ làm việc gì đó trước khi trạng thái hiện tại thay đổi.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
aa9ad514-a5b1-4496-a54c-28a876302983	grammar	～間に	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
b9791bc0-59fa-4f40-a0d5-22d5c8f3c7ae	grammar	～おかげで	\N	Nhờ có ...	N3	Diễn tả nguyên nhân dẫn đến một kết quả tốt.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
d187374f-c9a6-47f6-bcd5-9eb30b2ac601	grammar	～せいで	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
95ad4078-f7b0-4533-be8b-0db06ee4884b	grammar	～たばかり	\N	Vừa mới làm gì xong	N4	Diễn tả hành động vừa mới kết thúc trong cảm nhận của người nói.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
6490c6b4-a56b-429f-a1e9-3686955b3b3b	grammar	～たところ	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
26ba816e-ab5f-4794-a86c-8445f2b9364d	grammar	～によって	\N	Tùy vào / Bởi / Do	N3	Diễn tả phương thức, nguyên nhân hoặc sự thay đổi tùy theo đối tượng.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
7d567acb-c346-4309-ad84-578798877735	grammar	～から	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
faefb0eb-a835-4c6a-bee0-0b6188892387	grammar	～たとおりに	\N	Làm theo đúng như ...	N4	Diễn tả việc thực hiện một hành động y hệt như những gì đã thấy hoặc đã nghe.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
9eb78070-2982-499d-867b-1f08dc38e377	grammar	～ばいい	\N	Chỉ cần ... là được	N4	Đưa ra một giải pháp đơn giản để giải quyết vấn đề.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
f339e28a-4b40-4785-b366-7633dd7e4076	grammar	～たらいい	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
49bd1d8b-bd40-43d5-bd4b-2835bc05508d	grammar	～たほうがいい	\N	Nên làm gì đó	N4	Dùng để đưa ra lời khuyên mạnh mẽ hoặc so sánh giữa hai lựa chọn.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
9355d8d1-efe3-4ab8-ac14-cde87eb8f1b9	grammar	～たらどう	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
db2e3cd8-b301-4108-b24e-6bae845d445d	grammar	～までに	\N	Trước khi / Trước lúc (hạn chót)	N5	Diễn tả một thời hạn cuối cùng mà một hành động phải hoàn thành.	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
d4f519d6-4876-42d0-8ead-956f0162722b	grammar	～まで	\N		\N	\N	2026-04-12 10:53:25.817353+07	\N	\N	\N	\N
279dda32-0821-438e-8f35-4b873f615e3e	kanji	勉	ベン / つと.める	Miễn	N4	Hình ảnh sự nỗ lực, cố gắng hết sức.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
13809ec5-7c0a-47cb-aa10-53918124ae32	word	勤勉	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
a64d2073-21bf-4371-85cd-5ecfdd63a35a	kanji	免	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
ccb48f20-4f76-4eca-8157-403aebf74772	kanji	力	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0777dc70-893b-42e1-966f-26e39f9e3739	kanji	強	キョウ / つよ.い	Cường	N5	Hình ảnh cái cung mạnh mẽ và con côn trùng.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
6fa18c45-ee7a-473b-9051-eacf9e06fc82	word	強い	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
8ebd0e5a-49cb-4a19-a645-3086d7354b74	word	強調	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
075bc53b-99f3-436e-be3a-98055c1f9a57	word	強引	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
b005b84c-7345-448b-b02c-edfb31f38f86	kanji	弓	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
68a8fbd2-f2f9-4b4f-87aa-536ad62f0e19	kanji	口	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
4c2b4937-d3ae-4744-9af3-7a722d811ee9	kanji	虫	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
df351a21-3695-4b50-9cbe-6149a03c1670	kanji	病	ビョウ / やまい	Bệnh	N5	Hình ảnh người nằm trên giường bệnh.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
e5890c80-238d-4b73-902b-617ca2c706d1	word	病人	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c8f53251-2e15-4003-9393-168afecef27b	kanji	疒	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
f7a0da62-f0e2-41a9-9f46-bf8cd1a68d9f	kanji	丙	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
d50eba17-e1cd-4b30-ba66-647824a1475d	kanji	院	イン	Viện	N5	Nơi chốn, tòa nhà lớn (bệnh viện, học viện).	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
948b1eca-833a-4725-8f3e-6d051572b5bb	word	入院	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c66c189a-b313-4465-a2d9-9187b7ca1daa	word	退院	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
f95ea5ee-73df-4066-8b9a-107b81ea005b	kanji	⻖	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
162c8b97-bf3d-4f98-b59b-f4df1c56e522	kanji	完	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
1d4f9c26-b3d3-494c-ac63-887c207b6406	kanji	難	ナン / むずか.しい	Nan/Nạn	N3	Hình ảnh con chim bị mắc kẹt, tượng trưng cho khó khăn.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
18d2a4b9-8003-4a1d-9460-975a759c0b0e	word	災難	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
8d18673c-8fde-4aa8-b58b-37dc9f74bf3f	kanji	廿	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
473cb849-7799-4bc7-9162-e89b56011313	kanji	夫	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0c906103-1618-4699-ae19-d0d6b05ecdb2	kanji	隹	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
47540271-a252-4fbe-8021-5496944d1e3f	kanji	準	ジュン	Chuẩn	N4	Tiêu chuẩn hoặc chuẩn bị.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
d787f685-6ccf-4582-a2c8-27e4fe373734	word	準急	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
bec21e63-02dc-47ca-b50e-5893b9137884	kanji	水	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
45ed8edd-b903-44d6-b893-dab0b2076f3f	kanji	隼	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2f3c20b0-d8bf-4ece-a683-734faebaea30	kanji	備	ビ / そな.える	Bị	N3	Chuẩn bị đầy đủ đồ đạc, thiết bị.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3dccd4a5-413f-435d-86c7-0899f37e2e0e	word	備考	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
936c4b9e-a861-408f-b8f6-7ee7565932f5	kanji	人	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c20a3f89-9451-4925-a845-661336d00311	kanji	艹	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3da4f9d3-54a7-4b29-9331-7f3d44638653	kanji	厂	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
eac38814-9b74-4bc2-ad9a-ad7b58c7efd8	kanji	用	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
765ff007-7986-4956-b484-dd0ebe106e57	kanji	経	ケイ / へ.る	Kinh	N3	Trải qua thời gian hoặc kinh qua công việc.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
d4ca439a-ddac-4d32-ab35-f25aadf418b2	word	経営	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0e3b011c-0ec4-4036-a54f-1c633d6ba4c9	kanji	糸	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
dfea3499-c8ad-4c11-876b-a9b53c632b8b	kanji	圣	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
865c4ac3-e6ba-4545-a2d0-44c0962e3a20	kanji	験	ケン	Nghiệm	N4	Kiểm tra hoặc trải nghiệm thực tế.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
33768554-41bb-45cd-a59c-b2494b9e8b85	word	受験	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0017cbc6-69a5-4b25-9ad7-858c98a8fc10	kanji	馬	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0ae858d1-5db3-4246-a692-b5ef665d5a05	kanji	僉	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0fc2835c-d402-402a-9c31-c6da37e86bce	kanji	連	レン / つ.れる	Liên	N4	Sự kết nối, liên tục hoặc dẫn dắt.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3961deaf-f509-49ba-a13a-895e5f25e512	word	連続	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
00b9fbda-2b64-404c-80af-a38f8146c0fd	kanji	⻌	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
bf746ca2-89ac-4e62-8f12-aff76675b5d0	kanji	車	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	kanji	絡	ラク / から.む	Lạc	N3	Sợi dây liên kết hoặc quấn quýt.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
86ee567f-9ee4-462e-8d03-e68782b1bf5d	word	脈絡	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
efbcdbe0-c463-463c-ad0f-3ad4fec644a3	kanji	各	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
155926ec-47d3-4041-9a9d-3bb8389c1b20	kanji	忙	ボウ / いそが.しい	Mang	N4	Tâm trí bận rộn đến mức đánh mất mình.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
0e50ace3-335e-4332-a9c8-3010cd24bfff	kanji	忄	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
309ebd52-f82e-4111-bf46-2cb1862b95cd	kanji	亡	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
fd17d110-ae19-4d2e-b50f-e9283e5dd531	kanji	解	カイ / と.く	Giải	N3	Dùng dao để mổ xẻ, giải quyết vấn đề.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
1d94a57a-73e4-4cdc-aff6-6423202f3fb1	word	解説	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2fd07c1b-d3c5-41fb-add0-79d714fff578	word	理解	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
bba1550e-c5f6-41ad-97b4-8a50d02b4c8c	kanji	角	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3ba3e06e-137c-4e44-b5cd-0786ec9bfe6e	kanji	刀	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
bbee46ed-1761-4d16-93d5-a869ffebc3ce	kanji	牛	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
71c68e5c-38d1-447b-841a-18df37edab6d	kanji	決	ケツ / き.める	Quyết	N4	Quyết định một việc gì đó như dòng nước chảy.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
8d52bb39-8f51-4ec3-87ee-8c35c07c3d09	word	決して	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
315e1034-2742-45b4-b549-34522a66173a	kanji	夬	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
97504413-aa7f-487c-806f-55acf73f4756	kanji	興	キョウ	Hưng	N3	Sự hưng phấn, hứng thú hoặc thịnh vượng.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
ce4e9543-709e-4bab-9bb1-e9911b736ce6	word	興奮	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
789f9a91-aaea-4b09-85b1-4312a77397b2	kanji	臼	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2a261b53-f9b1-4e21-925c-ca23653b4fae	kanji	同	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
61d8620d-bb28-4ca1-9d2f-d9a8ca94930c	kanji	八	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2853c152-be27-43f0-94cf-5ceb051202d9	kanji	味	ミ / あじ	Vị	N4	Hương vị hoặc ý nghĩa của sự vật.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
a94b96d9-7743-4166-b161-b5c1fdd8674f	word	美味しい	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
6400fe4c-8226-4155-848a-254c81d24a53	kanji	未	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
bfb86f51-659f-4ea6-8e55-e2750ae90d96	kanji	申	シン / もう.す	Thân	N4	Bày tỏ, trình bày một cách khiêm tốn.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
318301cb-b75c-44e1-a90e-78c0d2d8f34f	word	申告	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
d44aa1bf-c7b8-4ed5-a703-840c73b969c4	kanji	田	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
4b64b45e-a701-44cc-ba36-bcc24c3971d0	kanji	丨	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
1550429a-13a2-4fed-8494-e88dd6c88ddd	kanji	上	ジョウ / うえ	Thượng	N5	Biểu đạt phía bên trên.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
acc0d4c2-e32f-4de7-af0b-57b0df876449	word	上手	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2558004b-653e-4f57-913b-cb3acd08ea26	word	上げる	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
744a896f-7075-4ac6-8753-33125aa9a337	kanji	一	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
263458cc-56c3-40e2-8bca-8b06089c17ea	kanji	卜	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
26bbdc4f-bc47-4b2a-9272-530166f4251c	kanji	食	ショク / た.べる	Thực	N5	Hình ảnh cái bát có nắp đậy, biểu thị việc ăn.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3b2b4470-dcc9-438c-af33-c1bef3b2427a	word	食べる	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
edc85707-8521-4d0a-a443-214a5932ec68	word	食堂	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
978408a0-de49-46e0-8241-61a256280e51	kanji	良	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
9de5a213-15c4-4cc4-836f-af2b0cf2bb26	kanji	事	ジ / こと	Sự	N5	Sự việc, công việc hoặc sự vật không hình dáng.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
7a9a424b-1da2-4a3e-84f8-c2a56b782b6e	word	食事	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
f7f1de32-12be-4ed3-87a8-5132db7a0021	word	大事	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
391c23bf-572a-42db-a7e7-bf05d099501c	word	行事	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c9b87791-2577-439f-a7c4-06afca4e0f25	kanji	彐	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
92a18f8b-adc1-4016-b19d-719e89ffba3b	kanji	亅	\N		\N	\N	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
3db95de6-b590-4eff-baf1-3e239689b322	word	勉強	べんきょう	Học tập	N5	Việc học tập hoặc nỗ lực tiếp thu kiến thức.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c97423bb-d7e6-4940-a239-1d2c1f685327	word	学習	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
b68c73e6-3791-4199-a954-c7b46fbd3f6e	word	学ぶ	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
2ed1fd6f-075c-474f-99a7-9352e29c4da9	word	遊ぶ	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
a87484bc-b8f2-44e3-80d6-8433f3e8d730	word	勉強不足	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
20671583-c9b8-48db-9ae2-d7e905dcfd3a	word	勉強家	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
07164d6b-941e-4f7b-ad9d-c80bacc93e8e	word	病院	びょういん	Bệnh viện	N5	Nơi khám và chữa bệnh.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
c7d33230-325e-412e-9136-51a542c15d7a	word	クリニック	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
36a01645-2635-497e-8863-81bb522f40f9	word	医院	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
9b520e99-8799-4e69-94c4-589dafa9f4a0	word	通院	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
11fcb009-8397-4770-a282-700a94910b07	word	難しい	むずかしい	Khó	N5	Không dễ dàng để thực hiện hoặc hiểu.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
838b532a-910a-4b16-a67f-7dd42bae9601	word	困難	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
20824390-eac6-4589-bb30-79841d314e72	word	大変	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
9a6e2cdb-62ca-4b3f-9aca-dac0f6fd1dcf	word	易しい	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
c7729558-0af7-4747-84b9-28fa0a3fa613	word	簡単	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
03ce4233-b134-4de9-bb16-5b27cba0f613	word	難しい顔	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	word	準備	じゅんび	Chuẩn bị	N4	Sắp xếp, chuẩn bị trước cho một sự kiện.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
fb90383e-011a-47d6-99a5-e8f69c3cf728	word	用意	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
bc5da74b-e3ab-421d-b804-56cccce46ab9	word	支度	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
f797d854-cf42-480c-b336-190cfdb3af23	word	準備運動	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
ea419bba-5ff9-42e7-8506-a93b1392c984	word	心の準備	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
774cff02-0a29-44af-a632-e8718658e0de	kanji	准	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
1b20c43a-739a-4eae-9593-00cd225e12ba	word	経験	けいけん	Kinh nghiệm	N3	Những kiến thức hoặc kỹ năng có được qua thực tế.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
341e8efd-7e3a-41bd-ac84-1553a250e2cb	word	体験	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
cc4fd81b-670a-45d2-9f30-960a15e5c938	word	実績	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
7b9afe68-e7e2-45d5-8daa-b8b6922af786	word	未経験	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
9747b240-c1c0-49d5-9530-65851ccbe1cc	word	経験者	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
84fac07e-8c47-4136-beda-30bfb27cfb33	word	連絡	れんらく	Liên lạc	N4	Thông báo hoặc trao đổi thông tin với người khác.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
1039429c-3f56-482a-9d35-5e58a6f7dad7	word	通知	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
17f7f16a-8cf1-4b54-a1ff-5a5eadc98997	word	報告	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
2e8d78c7-afcd-43ad-8052-d560ac6de964	word	連絡先	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
2e35f1ed-6beb-4f20-ab03-e5ab3860ef49	word	密に連絡	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	word	忙しい	いそがしい	Bận rộn	N5	Có nhiều việc phải làm, không có thời gian rảnh.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
923494f5-4093-4866-9708-01e85b89b252	word	多忙	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
eee468aa-47a6-4002-bb79-8f6a94902e57	word	暇	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
aac210c6-0ef7-47c4-bcc3-ad3db49fbe07	word	忙しそう	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	word	解決	かいけつ	Giải quyết	N3	Tìm ra câu trả lời hoặc xử lý xong một vấn đề.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
2855cf23-5e70-4042-8cbd-095389532ad7	word	処理	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
6e32be34-6bb5-4b20-87f1-46a552cd23fa	word	解消	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
7c7c4f65-1b86-4bf7-aef6-d36dc16b7e9a	word	解決策	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
4d7f0b57-6684-4318-bed8-9f7cb8090311	word	早期解決	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
bec461cd-95cc-4eec-99d0-201f1029484d	word	興味	きょうみ	Hứng thú/Quan tâm	N3	Sự quan tâm hoặc tò mò về một điều gì đó.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
eae59957-4a4f-4e4c-9cee-723857e87658	word	関心	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
f56b8d85-6fca-4e60-b157-4e29283c911b	word	好奇心	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
6126d949-ddbf-4bb6-bbfa-23c180c79a7b	word	無関心	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
557cddd7-2b45-4415-96d2-5ffd262ff506	word	興味深い	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
c8fc84e6-341f-47cf-b4af-308ff27e248c	word	無興味	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	word	申し上げる	もうしあげる	Nói/Trình bày (Khiêm nhường)	N4	Dạng khiêm nhường ngữ của động từ 'Nói'.	2026-04-12 10:53:25.842696+07	\N	\N	\N	\N
e6b9e039-52de-4434-b07b-b90d59d84d1b	word	言う	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
3c63318d-20a3-4dfd-97e1-377babed70c1	word	申す	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
460e2072-dddf-4ab7-927b-8d3338b27a6a	word	お願い申し上げる	\N		\N	\N	2026-04-12 10:53:25.898382+07	\N	\N	\N	\N
d11c9ef7-1f3d-4221-b7db-ffa9203a3524	kanji	図	ズ / はか.る	Đồ	N5	Hình ảnh bản đồ hoặc bản vẽ được bao quanh bởi khung.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
11fb167c-327d-45f1-9610-55c18c863ccb	word	図面	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
ac4120f9-c19a-44bf-9944-cc524196ad89	kanji	囗	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
5d239c61-3559-4b41-9d1b-ee0bc64b5e93	kanji	冬	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
6198b623-c6ec-4d23-a58c-546fa3ddfe3e	kanji	書	ショ / か.く	Thư	N5	Tay cầm bút lông để viết lên giấy.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9cbda39f-4123-4f36-8ef5-044ae0272394	word	書道	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
67e04771-c3f5-400c-96d6-670d10fd628b	word	辞書	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
266afaa2-2b4d-4afa-8b86-b9add9644bad	kanji	聿	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
a00fcc8e-3db5-405d-b182-1f612c25ee39	kanji	曰	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
995f94f1-08a5-4410-a982-1590fc0660af	kanji	館	カン	Quán	N4	Tòa nhà lớn dùng cho mục đích công cộng.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
8d6cc974-2d07-48dc-ad91-88ebcaad6fd0	word	大使館	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
df25f971-029b-4dba-b8ba-83749c182888	kanji	官	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
af3d39b9-ebff-4062-a292-ff27b0028800	kanji	散	サン / ち.る	Tán	N4	Làm rơi rụng, phân tán hoặc đi dạo.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
bde111cd-aaa7-4e03-a754-9bb1fbce1e44	word	分散	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
bdf568f7-b39b-4c3f-a682-7b24c11c060e	kanji	旦	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
4c33a6c6-3b71-4839-92ab-f2b19afb3c1c	kanji	月	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
d07ec3f9-6cf2-4ebf-8f78-782d78f98c9d	kanji	攵	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
fe926db8-7ef3-45a0-86be-6615013085b9	kanji	歩	ホ / ある.く	Bộ	N5	Hình ảnh hai bàn chân bước đi.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
80b2739f-ce9a-487b-8e37-f3d850d2c55b	word	一歩	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
f9df36b5-bdb1-4c97-9433-ea3678cb1c2e	kanji	止	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
d7877559-6cc6-4a1a-ac00-0ce52d231ed3	kanji	少	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
319744e3-d340-4cbe-8b71-bd8796d65a94	kanji	全	ゼン / まった.く	Toàn	N5	Sự đầy đủ, toàn bộ, không thiếu sót.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
c532f7c0-0075-4eec-ad39-869e0ee61317	word	全部	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
331255cb-e956-4f76-8119-8290f0a24abc	kanji	王	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	kanji	然	ゼン	Nhiên	N4	Tình trạng tự nhiên, hoặc đúng như vậy.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
f5669b9b-b90c-41af-b15f-436fe42ffe26	word	突然	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
d600860f-5f21-4d7f-8994-d2cd5e9e4e70	kanji	犬	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
ff800669-8146-4efa-bf21-47b79692dd67	kanji	灬	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	kanji	覚	カク / おぼ.える	Giác	N4	Cảm nhận, nhận thức hoặc tỉnh ngộ.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
d2e686e1-0bed-415b-adf9-c5d0137232e6	word	目覚まし	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
8e9be879-3d12-45e8-909f-584f18b2afe5	kanji	学	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
f9835727-aad2-48bb-8edf-28a8bbf049ae	kanji	見	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
835da823-14ea-4221-a420-30288dda0e97	kanji	相	ソウ / あい	Tương	N3	Sự hỗ trợ lẫn nhau hoặc bộ mặt của sự việc.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
3a9dc73a-22da-4c94-8b40-f07304c696d7	word	首相	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
993038e4-a435-44a4-988a-777d16777ba6	kanji	木	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
2dadcd7c-ba50-4157-b473-1c45c0ca5452	kanji	目	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
2c02eb98-f34b-495e-a410-aef3f25fc414	kanji	談	ダン	Đàm	N3	Nói chuyện, thảo luận sôi nổi.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9b814886-41dc-46ca-b14f-442ee814846f	word	会談	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
4eb3a489-ac3f-47ba-8f57-5148f55ef4c1	kanji	言	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
42002556-d14d-403e-961b-7e735f0f3142	kanji	炎	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
c16439b8-1685-449c-806f-cec26b1eec38	kanji	済	サイ / す.む	Tế	N3	Kết thúc, hoàn thành hoặc giúp đỡ xã hội.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
1a63f90f-49e3-4442-a8c0-1e6b803cd990	word	返済	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
0081cd8a-c4ac-4d03-a15e-7183e98d4627	kanji	齐	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
6bee9a87-fe8d-42fe-a1a0-814844a2d157	kanji	丁	テイ	Đinh	N4	Đơn vị khu phố hoặc số thứ tự.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
1f8aad77-1549-47ad-a892-993eb550e0ce	word	一丁目	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
b0d95268-5586-460e-9a28-a3cd08ef0a38	kanji	寧	ネイ	Ninh	N3	Sự yên tĩnh, lịch sự hoặc chu đáo.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
5a5cac90-9b4c-41e8-9c64-a36fd6b0a887	kanji	宀	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9077cd16-e36b-4bf0-a1de-1bc64c22ff76	kanji	心	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9ad2abab-9a2c-4400-8298-270f1ca2f606	kanji	皿	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
8e8358b9-428c-40fe-9815-a4fcce537b8d	kanji	習	シュウ / なら.う	Tập	N5	Học tập bằng cách thực hành lặp đi lặp lại.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
4c1a7005-1740-474d-a2de-1a06dd7b0027	word	練習	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
a50f4072-38fa-4a32-834b-0a6672910e26	kanji	羽	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
bc835f18-af16-4030-b21c-8a0c877b4b17	kanji	白	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
7dd74620-3716-4d35-ab6b-c37049467492	kanji	慣	カン / な.れる	Quán	N3	Quen với một việc gì đó qua thời gian.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
b96aed4c-3149-45ac-aa32-d7c2f0f0e216	word	不慣れ	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
46e8c4e2-7c15-4463-b4b3-c56f4ea4f10f	kanji	貫	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
37ff6d9e-f8f0-49de-afdc-53763905bb15	kanji	発	ハツ / た.つ	Phát	N4	Bắt đầu, rời đi hoặc công bố.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
9977c060-7b9e-41ef-bc6b-d9d4b3b99aba	word	発明	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
a3182274-f724-4a5d-acb5-bd849bd5d1ae	kanji	癶	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
2620c4b9-7d6c-49fd-9aea-586f76bf5a08	kanji	元	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
5774f626-5ef3-45fa-85c9-03fdf53fd778	kanji	表	ヒョウ / おもて	Biểu	N4	Bề mặt, bảng biểu hoặc thể hiện ra ngoài.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
4f3c2689-b073-4316-9f56-c58e02563767	word	表情	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
60864f94-2107-4f7f-bcef-94a0f9e80b5c	kanji	衣	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
fe13c5a5-ee85-4666-ac85-89aac99a7a9b	kanji	二	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
30da8d06-0ffc-459f-ab8a-0651fe92082b	kanji	忘	ボウ / わす.れる	Vong	N5	Quên mất, không còn trong tâm trí.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
74ff495b-9753-41ed-92c3-d81af032ea28	word	忘れ物	\N		\N	\N	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
1a30b075-876e-4204-bf12-a981bb7e86cd	word	図書館	としょかん	Thư viện	N5	Tòa nhà hoặc nơi lưu giữ nhiều sách để đọc và mượn.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
88ddf1c4-499f-40b5-b8f5-723b6b76dbf3	word	図書室	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
93c5fa36-1387-44bf-875f-5f2ae40c5e92	word	図書館員	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
68be6074-44d5-4297-86d8-250d5760669a	word	市立図書館	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
3782b95a-537a-4459-ba1e-9dc3308868d0	word	散歩	さんぽ	Đi dạo	N5	Việc đi bộ thong thả để thư giãn hoặc tập thể dục.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
cdad58fe-8a82-4421-8f2d-da6dc66c986e	word	ウォーキング	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
54c1e73a-22e9-4af0-8158-bb7ccfd16593	word	散歩道	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
0d1fe0f0-67f6-49bf-8274-1602f5242c1d	word	朝の散歩	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
252a4c0c-4a5d-490e-adc9-c004ad3f7694	word	全然	ぜんぜん	Hoàn toàn (không)	N4	Phó từ dùng để nhấn mạnh sự phủ định hoàn toàn.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
4cf26c78-0567-4e62-ae2f-5df978a81b1d	word	全く	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
55c2e451-9bca-45ab-b9d5-cb154fed900c	word	さっぱり	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
ec694d9f-8b97-44d7-8414-33cf9bb50e1e	word	よく	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
c45097bb-9122-447c-ab24-baec38948e69	word	だいぶ	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
8c0f5eab-347d-4d5e-beb8-f899b5509b51	word	覚える	おぼえる	Nhớ/Ghi nhớ/Học thuộc	N4	Tiếp nhận thông tin và lưu giữ trong trí nhớ.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
57b67ad8-56b3-4b65-ada9-53edcd75533a	word	記憶する	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
34e097b0-bee6-4593-a8d3-a58c162181e9	word	暗記する	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
935008fd-bf1f-4a3d-a458-4f08ffda91f4	word	見覚える	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
4f665837-e3fb-4652-a87a-99014b21b89d	word	身に覚える	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
268f9c48-1f0f-49ac-a065-281e645009f1	word	相談	そうだん	Thảo luận/Bàn bạc/Hỏi ý kiến	N3	Trao đổi ý kiến với người khác để giải quyết vấn đề hoặc đưa ra quyết định.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
bbbe71e6-0a7c-47e9-bc60-6c1b8893b7ef	word	協議	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
d9a2edb4-a02a-4001-a8b2-5140cd3837ad	word	話し合い	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
2fd9212d-da95-4ede-812f-b34b7a76d28a	word	相談窓口	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
c33f26bf-2ade-4f69-b5c8-2c936819f5c7	word	人生相談	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
6474854c-a582-44d9-8e30-594f61d63b81	word	経済	けいざい	Kinh tế	N3	Hệ thống sản xuất, phân phối và tiêu dùng hàng hóa/dịch vụ.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
b12c1e9f-36ba-450e-969b-c3f110b4d9ca	word	経済学者	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
ad480e92-67ca-4f84-9bd9-bd2b430996fb	word	経済成長	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
652cc60f-9a1e-4120-8ebd-1297daecea82	word	経済的	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
b6d50bc3-2fbc-458a-8027-a34f72da42c1	word	丁寧	ていねい	Lịch sự/Cẩn thận	N4	Thái độ tôn trọng người khác hoặc làm việc một cách tỉ mỉ.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
11e23ac6-3e7e-47f1-abee-ab74205af51c	word	親切	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
9bcb2bd8-1a5f-418c-a9ec-99273510d4ac	word	慎重	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
3ccef624-aec0-4097-9156-e06c7a0324c7	word	失礼	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
f2cbd464-a002-4571-891e-e8b26b0f43e1	word	雑	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
483c7869-77ab-437d-87ae-2130fd26fb8a	word	丁寧語	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
46e7c181-4dba-402e-a903-6f249253080a	word	習慣	しゅうかん	Thói quen/Tập quán	N3	Hành động lặp đi lặp lại hàng ngày hoặc phong tục của một xã hội.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
bf71c34a-7e17-44de-b076-b84a92a1e382	word	習わし	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
9a5f75b6-0b25-403b-8b3f-8b0fa4eb7270	word	慣習	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
7e7b1c70-d795-4054-bcf4-de10aab9b1b8	word	生活習慣	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
2a99a105-8ce8-4691-9b81-61145f901796	word	習慣づける	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
c0656f9d-b3ec-4f2e-8763-7a25033d1157	word	発表	はっぴょう	Phát biểu/Công bố/Thuyết trình	N3	Đưa thông tin hoặc kết quả nghiên cứu ra cho nhiều người biết.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
20195b29-7239-4952-8a8e-5b17bf3b5cfb	word	公開	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
d583902a-337f-4e3c-be5c-de5fc99d9a48	word	プレゼン	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
84b35d95-a556-4d6d-b011-04ab37c23cd5	word	発表会	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
9905f14d-4f83-4612-8721-cd3c5665dcb5	word	公式発表	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
54d3aeca-293e-4873-8c10-0122c492f361	word	忘れる	わすれる	Quên	N5	Không còn nhớ hoặc để sót lại một vật gì đó.	2026-04-12 10:53:25.929457+07	\N	\N	\N	\N
03abe315-7883-4b31-a615-4fc8ab4fea46	word	失念する	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
55283de8-eb01-4367-b8fe-ef74ff8d4759	word	思い出す	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
ca20bc13-4746-44eb-bdfa-6c9fb3be5365	word	忘れがたい	\N		\N	\N	2026-04-12 10:53:25.968517+07	\N	\N	\N	\N
217bd970-affb-4c92-8ba3-a96fc9c924a4	word	テスト語	てすとご	từ kiểm thử (đã cập nhật)	N4	Chỉ dùng để test	2026-04-13 15:08:04.957362+07	\N	\N	danh từ	2026-04-13 15:11:21.99206+07
\.


--
-- Data for Name: entry_relations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.entry_relations (id, source_id, target_id, relation_type) FROM stdin;
08a28532-f5d6-4d4c-bd46-31c163907e19	4b781e2f-06da-4af0-9ee1-db12231d67e5	15540844-6077-4fa5-a8dd-ad3d61d6386e	synonym
9795ed99-8daf-4032-8c35-de6c66e454bd	4b781e2f-06da-4af0-9ee1-db12231d67e5	330fa901-e35f-46b9-9fab-9006a7a06b62	synonym
6b8e069c-52ff-45dc-a02f-4daa678d1db8	48768c0f-626c-4562-9e75-d8886d1042d7	95ad4078-f7b0-4533-be8b-0db06ee4884b	synonym
b73ae450-0b16-4d4c-b360-309e068f062c	187c683c-8c8d-4c0a-9059-64cd4f8814bb	38042282-4946-49ee-995b-0a08edd2734a	synonym
51758a36-0a13-4cf2-8612-34f6ada36387	187c683c-8c8d-4c0a-9059-64cd4f8814bb	06316ec8-de6f-4033-a388-c0e943c9bb13	synonym
fabf025e-b1de-4318-a8c6-a32faf212cb5	cc54c6e3-12b6-4923-87fa-711b21cc8019	050dc7d6-d14c-4dd5-88b4-0771f79d31ac	synonym
bcc36d32-53c3-4bc7-ae7d-b64f8243f42d	43ec397a-37a2-433c-935e-40495fe27e27	d250088b-8180-490b-b14a-4663cf6e10c0	synonym
c383c97e-e082-4fea-b4ce-ac78fe8bd708	bcbe9058-efdf-430b-960a-06b04e043201	c605c2da-f07a-4da0-b43b-030e92d70836	synonym
b7fa74de-2bfd-4585-8b3c-bc12cff951cc	bcbe9058-efdf-430b-960a-06b04e043201	4c11c0ad-7bd5-4c8b-9b1b-e660baba0d52	synonym
53de553c-4bdb-42cc-b139-bc2e018192a7	6f2071da-85f7-4a3f-a0a0-0a09592fc172	7b381c3a-96da-4388-8716-7f5b3311d854	synonym
251e49fc-c397-4931-8594-9ff672c3020d	6f2071da-85f7-4a3f-a0a0-0a09592fc172	bfc7fceb-b63d-4de8-8bd1-05d4890b955b	synonym
d1733cf5-34ac-479a-a952-3f8d34414321	4b36d5e8-0617-41af-bb0b-51b85e7a15a4	1f4e65df-2ade-4f66-9af4-5f70118c7f5c	synonym
13b6eaf1-9dc7-45f8-bfd4-3b0022c0e07d	4b36d5e8-0617-41af-bb0b-51b85e7a15a4	12206495-83bd-4795-a1e6-137af1a256a5	synonym
578bf9d5-e66c-4bf2-abef-0565c850b283	f5af8af6-b458-48c2-9289-89611f584314	7d1fdb96-39ab-405f-93a8-09f1bde61041	synonym
37441b22-f522-4a7c-9f0f-976910ebbbf7	f5af8af6-b458-48c2-9289-89611f584314	dc173ead-9c67-4580-af8a-c3ccb2c7d41b	synonym
a8a389ce-f8f4-4a0d-b679-f754b22ac214	d028bc1f-cc5f-4b55-a526-abb4950e5547	87fd6264-0cda-48f3-80a1-1fbca1108c19	synonym
52307441-4c80-4654-aa3c-a8173fa0e0e8	d028bc1f-cc5f-4b55-a526-abb4950e5547	6619874a-73a1-4125-ae08-882f2f5364c1	synonym
9b5f007d-97a3-45ef-90dd-35231e4a85e1	74b7a2ad-edb0-4b22-bc9e-8874a277e517	fda4b62c-53ab-4284-b45b-e61e284542c6	synonym
e73bc1d0-13bd-4c99-af76-330aec858ac4	40d42513-b5ed-45b5-9d2b-62aa752cfaf1	14ddbdc9-af90-4f77-be10-4feef161596f	synonym
4821c09d-fa21-4119-883e-603aeadcf2ba	40d42513-b5ed-45b5-9d2b-62aa752cfaf1	563d7219-c40b-4a16-9883-f42113b6e57f	synonym
64d28d75-1851-4f83-852a-25dc50695953	3ce52a73-530f-4218-a1bb-130869504339	aa9ad514-a5b1-4496-a54c-28a876302983	synonym
8b6fa43d-99e6-4f2d-a708-93ce9286fb04	b9791bc0-59fa-4f40-a0d5-22d5c8f3c7ae	d187374f-c9a6-47f6-bcd5-9eb30b2ac601	synonym
268fde68-ee36-4fad-b3e7-64fff09e96a5	95ad4078-f7b0-4533-be8b-0db06ee4884b	6490c6b4-a56b-429f-a1e9-3686955b3b3b	synonym
96ffd437-0bde-45e4-9cab-73e2e8dd6ff6	26ba816e-ab5f-4794-a86c-8445f2b9364d	7d567acb-c346-4309-ad84-578798877735	synonym
97ae93be-53aa-4c4b-9a29-ef7fa0e034a3	faefb0eb-a835-4c6a-bee0-0b6188892387	fda4b62c-53ab-4284-b45b-e61e284542c6	synonym
929e16c3-9d5b-46b9-b360-4f11b1120eab	9eb78070-2982-499d-867b-1f08dc38e377	f339e28a-4b40-4785-b366-7633dd7e4076	synonym
a37a891b-bca5-4f71-8481-36857b2e50a9	49bd1d8b-bd40-43d5-bd4b-2835bc05508d	9355d8d1-efe3-4ab8-ac14-cde87eb8f1b9	synonym
6efd2033-a228-48f4-9531-a8d5688614d2	db2e3cd8-b301-4108-b24e-6bae845d445d	d4f519d6-4876-42d0-8ead-956f0162722b	synonym
b2d9e959-1253-4a55-8585-a38ff8ab3e76	279dda32-0821-438e-8f35-4b873f615e3e	3db95de6-b590-4eff-baf1-3e239689b322	compound
b57f735a-e4f4-4dfe-9cd3-5febd641d674	279dda32-0821-438e-8f35-4b873f615e3e	13809ec5-7c0a-47cb-aa10-53918124ae32	compound
26d66d36-0930-4b61-adf6-e50ed9c83c6f	279dda32-0821-438e-8f35-4b873f615e3e	a64d2073-21bf-4371-85cd-5ecfdd63a35a	radical
63d35257-856e-4a0c-90cc-1cd3008ee094	279dda32-0821-438e-8f35-4b873f615e3e	ccb48f20-4f76-4eca-8157-403aebf74772	radical
c6663809-9f60-4a5e-aa6c-49e0deb8edec	0777dc70-893b-42e1-966f-26e39f9e3739	6fa18c45-ee7a-473b-9051-eacf9e06fc82	compound
5201524f-2565-451d-a3fd-aa826d373f67	0777dc70-893b-42e1-966f-26e39f9e3739	8ebd0e5a-49cb-4a19-a645-3086d7354b74	compound
19f0802a-4655-4b83-a9a0-adb7eba66f08	0777dc70-893b-42e1-966f-26e39f9e3739	075bc53b-99f3-436e-be3a-98055c1f9a57	compound
670925d8-8603-499d-a61d-77ccff4c4786	0777dc70-893b-42e1-966f-26e39f9e3739	b005b84c-7345-448b-b02c-edfb31f38f86	radical
31e70a43-49ea-422d-a393-3c10c23369cc	0777dc70-893b-42e1-966f-26e39f9e3739	68a8fbd2-f2f9-4b4f-87aa-536ad62f0e19	radical
43491bf4-dac9-47a9-afed-f8a9eb3e3701	0777dc70-893b-42e1-966f-26e39f9e3739	4c2b4937-d3ae-4744-9af3-7a722d811ee9	radical
01e61c4d-d9a4-4600-8a3c-59320d2b778c	df351a21-3695-4b50-9cbe-6149a03c1670	e5890c80-238d-4b73-902b-617ca2c706d1	compound
e0f88aa4-c045-4144-8f73-221ea53fcea6	df351a21-3695-4b50-9cbe-6149a03c1670	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	compound
a27526e0-cf44-4533-ae37-a28a645e241c	df351a21-3695-4b50-9cbe-6149a03c1670	c8f53251-2e15-4003-9393-168afecef27b	radical
c41d8016-ea1c-4492-a637-797c9d7b622d	df351a21-3695-4b50-9cbe-6149a03c1670	f7a0da62-f0e2-41a9-9f46-bf8cd1a68d9f	radical
51a70132-b6b8-4e18-8952-d17d6e19c1c5	d50eba17-e1cd-4b30-ba66-647824a1475d	948b1eca-833a-4725-8f3e-6d051572b5bb	compound
0d16b20d-c1b4-4f09-8da0-3131420f529f	d50eba17-e1cd-4b30-ba66-647824a1475d	c66c189a-b313-4465-a2d9-9187b7ca1daa	compound
8bf1cd92-7ff1-4b88-8b95-007d66e23838	d50eba17-e1cd-4b30-ba66-647824a1475d	f95ea5ee-73df-4066-8b9a-107b81ea005b	radical
ab72a9d3-c458-4a0c-8f26-b36b36c814cd	d50eba17-e1cd-4b30-ba66-647824a1475d	162c8b97-bf3d-4f98-b59b-f4df1c56e522	radical
0ff01bb9-7701-4c16-b214-296ded995bf0	1d4f9c26-b3d3-494c-ac63-887c207b6406	11fcb009-8397-4770-a282-700a94910b07	compound
fbff4610-9da7-4141-bf83-5b68c02d18a0	1d4f9c26-b3d3-494c-ac63-887c207b6406	18d2a4b9-8003-4a1d-9460-975a759c0b0e	compound
94119596-5f51-4a64-88ba-b9e84e51328f	1d4f9c26-b3d3-494c-ac63-887c207b6406	8d18673c-8fde-4aa8-b58b-37dc9f74bf3f	radical
d561df2e-8eb5-4b0f-937d-eb874c8fd032	1d4f9c26-b3d3-494c-ac63-887c207b6406	473cb849-7799-4bc7-9162-e89b56011313	radical
1b654c7f-b2b5-46db-8a4c-ba8d1108c758	1d4f9c26-b3d3-494c-ac63-887c207b6406	0c906103-1618-4699-ae19-d0d6b05ecdb2	radical
8f5641c6-f102-47fc-99c1-6014066be3fb	47540271-a252-4fbe-8021-5496944d1e3f	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	compound
d40eeadc-d7cd-4943-b6b4-aba6a6608b40	47540271-a252-4fbe-8021-5496944d1e3f	d787f685-6ccf-4582-a2c8-27e4fe373734	compound
5ae43472-4f54-4999-93e4-91e067ba0d85	47540271-a252-4fbe-8021-5496944d1e3f	bec21e63-02dc-47ca-b50e-5893b9137884	radical
2a513de7-edb8-4fe4-bd22-c2f0ac8c425d	47540271-a252-4fbe-8021-5496944d1e3f	45ed8edd-b903-44d6-b893-dab0b2076f3f	radical
6d80c33c-806d-490b-aeae-1e764a007c87	2f3c20b0-d8bf-4ece-a683-734faebaea30	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	compound
ad0a8db3-33bc-4a77-80bc-187980d17412	2f3c20b0-d8bf-4ece-a683-734faebaea30	3dccd4a5-413f-435d-86c7-0899f37e2e0e	compound
6c2778cd-e725-4869-9099-9cca9e7b4d93	2f3c20b0-d8bf-4ece-a683-734faebaea30	936c4b9e-a861-408f-b8f6-7ee7565932f5	radical
15ea909f-9471-45c0-959a-99f9c42e2c52	2f3c20b0-d8bf-4ece-a683-734faebaea30	c20a3f89-9451-4925-a845-661336d00311	radical
fb3f923d-6eb9-4da1-8a5a-c41c5afc6121	2f3c20b0-d8bf-4ece-a683-734faebaea30	3da4f9d3-54a7-4b29-9331-7f3d44638653	radical
d6385375-530c-4714-9c19-4d36d047e475	2f3c20b0-d8bf-4ece-a683-734faebaea30	eac38814-9b74-4bc2-ad9a-ad7b58c7efd8	radical
1070370e-ae93-4ecf-8d3f-5d665a9de2ae	765ff007-7986-4956-b484-dd0ebe106e57	1b20c43a-739a-4eae-9593-00cd225e12ba	compound
730c0870-fcb6-4c6b-8d80-2c9b138e7a79	765ff007-7986-4956-b484-dd0ebe106e57	d4ca439a-ddac-4d32-ab35-f25aadf418b2	compound
f943814c-35c8-4317-b514-c7c0376b1dd1	765ff007-7986-4956-b484-dd0ebe106e57	0e3b011c-0ec4-4036-a54f-1c633d6ba4c9	radical
6beeb479-6b01-4d5d-81bd-661079e96282	765ff007-7986-4956-b484-dd0ebe106e57	dfea3499-c8ad-4c11-876b-a9b53c632b8b	radical
09213783-8911-4014-9a5c-e693dc6f7e60	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	1b20c43a-739a-4eae-9593-00cd225e12ba	compound
41f7ffdc-61de-4f79-86f9-69b1167273d4	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	33768554-41bb-45cd-a59c-b2494b9e8b85	compound
c2121d9b-68b2-4487-9699-4bef3ecce491	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	0017cbc6-69a5-4b25-9ad7-858c98a8fc10	radical
79cb40fb-c32c-4e6e-8a52-dfa3d87d95a0	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	0ae858d1-5db3-4246-a692-b5ef665d5a05	radical
2659f421-718b-44b3-a74d-c04db6e8fcd7	0fc2835c-d402-402a-9c31-c6da37e86bce	84fac07e-8c47-4136-beda-30bfb27cfb33	compound
2fea2656-ac57-4a3e-a100-5ba9907aa230	0fc2835c-d402-402a-9c31-c6da37e86bce	3961deaf-f509-49ba-a13a-895e5f25e512	compound
a732f317-ef36-4b69-9d49-6bf4e1e30946	0fc2835c-d402-402a-9c31-c6da37e86bce	00b9fbda-2b64-404c-80af-a38f8146c0fd	radical
041c60e5-ee8e-4630-9c95-38871aa50194	0fc2835c-d402-402a-9c31-c6da37e86bce	bf746ca2-89ac-4e62-8f12-aff76675b5d0	radical
a3e6d1bb-ff20-4d38-9be5-888d87347c67	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	84fac07e-8c47-4136-beda-30bfb27cfb33	compound
9ed89608-a794-44f7-be3b-31d984703e52	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	86ee567f-9ee4-462e-8d03-e68782b1bf5d	compound
70cabe2e-9d82-40bf-9981-0a7bf0268abc	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	0e3b011c-0ec4-4036-a54f-1c633d6ba4c9	radical
6cc7a8e6-9ff4-470c-8361-0fb45a34b54a	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	efbcdbe0-c463-463c-ad0f-3ad4fec644a3	radical
b8ce2ac3-1cd7-4c72-a8b3-fbce878be578	155926ec-47d3-4041-9a9d-3bb8389c1b20	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	compound
03c77763-27d4-4ad4-83ff-f8b2146a8217	155926ec-47d3-4041-9a9d-3bb8389c1b20	0e50ace3-335e-4332-a9c8-3010cd24bfff	radical
088bfdbb-c869-4c40-8f27-44fc8c5c6980	155926ec-47d3-4041-9a9d-3bb8389c1b20	309ebd52-f82e-4111-bf46-2cb1862b95cd	radical
1efc653b-c7c4-4302-b45b-80608855716d	fd17d110-ae19-4d2e-b50f-e9283e5dd531	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	compound
3a0ce15f-1a5c-4834-936b-0daaee54e997	fd17d110-ae19-4d2e-b50f-e9283e5dd531	1d94a57a-73e4-4cdc-aff6-6423202f3fb1	compound
03ec24d5-3386-4cfc-bb3d-721876e43f21	fd17d110-ae19-4d2e-b50f-e9283e5dd531	2fd07c1b-d3c5-41fb-add0-79d714fff578	compound
a4e210ef-ad1c-4766-ad8e-fdeafd0f8cdb	fd17d110-ae19-4d2e-b50f-e9283e5dd531	bba1550e-c5f6-41ad-97b4-8a50d02b4c8c	radical
7fc8c483-df5b-40e0-8bf9-435cde20ac0a	fd17d110-ae19-4d2e-b50f-e9283e5dd531	3ba3e06e-137c-4e44-b5cd-0786ec9bfe6e	radical
23328dcc-7e3b-46dd-9a99-f00542b3ad0b	fd17d110-ae19-4d2e-b50f-e9283e5dd531	bbee46ed-1761-4d16-93d5-a869ffebc3ce	radical
18917a86-6a8c-42a0-b8a3-0b305d47b35d	71c68e5c-38d1-447b-841a-18df37edab6d	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	compound
d2d5e115-c91f-4045-ad74-a9c7420d1603	71c68e5c-38d1-447b-841a-18df37edab6d	8d52bb39-8f51-4ec3-87ee-8c35c07c3d09	compound
b2d4bc47-d003-43a5-9466-fa76665bbf76	71c68e5c-38d1-447b-841a-18df37edab6d	bec21e63-02dc-47ca-b50e-5893b9137884	radical
54cf418b-b6a0-43be-b1cb-2c067f6a25a5	71c68e5c-38d1-447b-841a-18df37edab6d	315e1034-2742-45b4-b549-34522a66173a	radical
4bae42b0-a21d-4a2e-9cd1-7f82eff84c83	97504413-aa7f-487c-806f-55acf73f4756	bec461cd-95cc-4eec-99d0-201f1029484d	compound
b1d39e1f-e5aa-4c99-a080-37067e749816	97504413-aa7f-487c-806f-55acf73f4756	ce4e9543-709e-4bab-9bb1-e9911b736ce6	compound
4b8cf7de-fc4b-4436-bc16-648226fb760c	97504413-aa7f-487c-806f-55acf73f4756	789f9a91-aaea-4b09-85b1-4312a77397b2	radical
e4f4a035-471a-4423-b2e4-1238144af018	97504413-aa7f-487c-806f-55acf73f4756	2a261b53-f9b1-4e21-925c-ca23653b4fae	radical
7f97f131-610a-4418-a228-0d1309c76394	97504413-aa7f-487c-806f-55acf73f4756	61d8620d-bb28-4ca1-9d2f-d9a8ca94930c	radical
bbe2493c-dacd-4a82-a409-10ea6026bcdc	2853c152-be27-43f0-94cf-5ceb051202d9	bec461cd-95cc-4eec-99d0-201f1029484d	compound
eccb54ff-3978-40a5-8e3d-360e240c76b0	2853c152-be27-43f0-94cf-5ceb051202d9	a94b96d9-7743-4166-b161-b5c1fdd8674f	compound
250dc102-2f7b-4626-b21c-0d4384509e2e	2853c152-be27-43f0-94cf-5ceb051202d9	68a8fbd2-f2f9-4b4f-87aa-536ad62f0e19	radical
069a8608-bfcd-47c5-849a-2288d5afa985	2853c152-be27-43f0-94cf-5ceb051202d9	6400fe4c-8226-4155-848a-254c81d24a53	radical
5ee1a6d0-7a1f-4a72-a2f5-8e97fbd5687e	bfb86f51-659f-4ea6-8e55-e2750ae90d96	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	compound
d1069ce5-8527-4278-8410-d03b27c002ac	bfb86f51-659f-4ea6-8e55-e2750ae90d96	318301cb-b75c-44e1-a90e-78c0d2d8f34f	compound
d5addc6f-ba6c-40d6-b4a8-f51365b24d48	bfb86f51-659f-4ea6-8e55-e2750ae90d96	d44aa1bf-c7b8-4ed5-a703-840c73b969c4	radical
ed816f3e-bca3-4c6f-8d6b-9df02925a52d	bfb86f51-659f-4ea6-8e55-e2750ae90d96	4b64b45e-a701-44cc-ba36-bcc24c3971d0	radical
5ed7eba9-1c5c-424f-82ce-27c1c1e467d4	1550429a-13a2-4fed-8494-e88dd6c88ddd	acc0d4c2-e32f-4de7-af0b-57b0df876449	compound
8400a5aa-9679-4c5e-a2b0-364a35db67ac	1550429a-13a2-4fed-8494-e88dd6c88ddd	2558004b-653e-4f57-913b-cb3acd08ea26	compound
5bf4e485-bbe8-44a3-9496-3cb7d55b9859	1550429a-13a2-4fed-8494-e88dd6c88ddd	744a896f-7075-4ac6-8753-33125aa9a337	radical
538ccc26-beab-4d8f-80f8-d439f55e9e63	1550429a-13a2-4fed-8494-e88dd6c88ddd	263458cc-56c3-40e2-8bca-8b06089c17ea	radical
f36ed1e6-7800-49e0-beeb-4c18b22e82d1	26bbdc4f-bc47-4b2a-9272-530166f4251c	3b2b4470-dcc9-438c-af33-c1bef3b2427a	compound
e3e71eb2-671d-42ce-a6f9-a2487bd9bb7f	26bbdc4f-bc47-4b2a-9272-530166f4251c	edc85707-8521-4d0a-a443-214a5932ec68	compound
0f086ed3-c2a6-4935-8111-c517179c74e6	26bbdc4f-bc47-4b2a-9272-530166f4251c	936c4b9e-a861-408f-b8f6-7ee7565932f5	radical
c751a7dd-1b5b-44e1-baf8-8542c7b4d151	26bbdc4f-bc47-4b2a-9272-530166f4251c	978408a0-de49-46e0-8241-61a256280e51	radical
fc7d39b0-f322-4ff4-ae2d-61442b9eb381	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	7a9a424b-1da2-4a3e-84f8-c2a56b782b6e	compound
fe6f9af6-d5d1-41e7-8ee0-bafe76ce0de6	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	f7f1de32-12be-4ed3-87a8-5132db7a0021	compound
e0eee20d-de90-4a52-b49e-65aff986eac3	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	391c23bf-572a-42db-a7e7-bf05d099501c	compound
9f70c35a-5c16-4c03-a8fe-55f5e266797c	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	744a896f-7075-4ac6-8753-33125aa9a337	radical
e3b9459c-d53c-4f49-ae7b-08bce73ba617	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	68a8fbd2-f2f9-4b4f-87aa-536ad62f0e19	radical
c8b458b3-cf0e-4045-aa47-ca81cff6ad4d	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	c9b87791-2577-439f-a7c4-06afca4e0f25	radical
7f423148-a510-4658-83ea-1bf408decfb1	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	92a18f8b-adc1-4016-b19d-719e89ffba3b	radical
f16d5fb6-fb3a-4cec-89bf-7609a8615daa	3db95de6-b590-4eff-baf1-3e239689b322	c97423bb-d7e6-4940-a239-1d2c1f685327	synonym
08ca5410-d69b-4421-98be-dcacbdab38c4	3db95de6-b590-4eff-baf1-3e239689b322	b68c73e6-3791-4199-a954-c7b46fbd3f6e	synonym
da14955e-0caa-4e56-86b0-cfe78ba8e1b4	3db95de6-b590-4eff-baf1-3e239689b322	2ed1fd6f-075c-474f-99a7-9352e29c4da9	antonym
8596531b-a15d-4245-a697-315d7e034454	3db95de6-b590-4eff-baf1-3e239689b322	a87484bc-b8f2-44e3-80d6-8433f3e8d730	compound
bb9965fd-4d87-4567-b564-da20600cc347	3db95de6-b590-4eff-baf1-3e239689b322	20671583-c9b8-48db-9ae2-d7e905dcfd3a	compound
cd700664-586f-46ef-9741-a59ca7b363b8	3db95de6-b590-4eff-baf1-3e239689b322	279dda32-0821-438e-8f35-4b873f615e3e	kanji
6cfc44be-e2b2-479f-99a3-f91e3fe0e8b8	3db95de6-b590-4eff-baf1-3e239689b322	0777dc70-893b-42e1-966f-26e39f9e3739	kanji
c0615f38-2db2-406a-933a-93b93bfffaf3	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	c7d33230-325e-412e-9136-51a542c15d7a	synonym
5125e31d-43e8-4d6c-8fa6-952b59fd416b	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	36a01645-2635-497e-8863-81bb522f40f9	synonym
c6f12365-da1c-44f4-93b6-55d6a266a4dc	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	948b1eca-833a-4725-8f3e-6d051572b5bb	compound
733d4f14-de29-4c6c-b381-f3be4c930809	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	c66c189a-b313-4465-a2d9-9187b7ca1daa	compound
9f2c27d8-4f82-42d5-85f0-dfb72dbb4c88	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	9b520e99-8799-4e69-94c4-589dafa9f4a0	compound
e28db797-84b2-4cb4-a820-9cc2da6bd26c	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	df351a21-3695-4b50-9cbe-6149a03c1670	kanji
fb6c33a5-3ea6-43b4-8fa7-3b3f3871645c	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	d50eba17-e1cd-4b30-ba66-647824a1475d	kanji
7ca37f6e-06f2-4444-a07a-b5e76363e4fa	11fcb009-8397-4770-a282-700a94910b07	838b532a-910a-4b16-a67f-7dd42bae9601	synonym
e9ed2c6a-40c1-41ca-98ab-6102b64c3c14	11fcb009-8397-4770-a282-700a94910b07	20824390-eac6-4589-bb30-79841d314e72	synonym
6c42162d-edb2-4bd5-bd0f-a4ad3ab96065	11fcb009-8397-4770-a282-700a94910b07	9a6e2cdb-62ca-4b3f-9aca-dac0f6fd1dcf	antonym
af3c0d3d-a97f-4410-8d35-92d87bd0baac	11fcb009-8397-4770-a282-700a94910b07	c7729558-0af7-4747-84b9-28fa0a3fa613	antonym
b7f9fd1d-45ec-46a9-9699-3ad4e934aaa3	11fcb009-8397-4770-a282-700a94910b07	03ce4233-b134-4de9-bb16-5b27cba0f613	compound
14a3c283-7a3d-414a-bb4c-4ee9637694fc	11fcb009-8397-4770-a282-700a94910b07	1d4f9c26-b3d3-494c-ac63-887c207b6406	kanji
c5c964c5-b546-44dc-88ee-79c7e5138923	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	fb90383e-011a-47d6-99a5-e8f69c3cf728	synonym
e5e96ec7-b668-4aa1-a71f-2ec49ae1ab55	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	bc5da74b-e3ab-421d-b804-56cccce46ab9	synonym
0c39e63c-8599-466f-8c9e-a05f6c7132ba	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	f797d854-cf42-480c-b336-190cfdb3af23	compound
b908f89e-1e99-49a4-9ade-58646c4082a3	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	ea419bba-5ff9-42e7-8506-a93b1392c984	compound
971bc677-9af9-4ccd-89b8-ec0b4dda7b69	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	774cff02-0a29-44af-a632-e8718658e0de	kanji
46306e31-bee1-4966-a610-82c77e2a2aff	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	2f3c20b0-d8bf-4ece-a683-734faebaea30	kanji
2938e1e6-25a4-4372-b19a-75b7e10a4cdf	1b20c43a-739a-4eae-9593-00cd225e12ba	341e8efd-7e3a-41bd-ac84-1553a250e2cb	synonym
dbfe240b-0995-48f4-af86-8c1bed4453e6	1b20c43a-739a-4eae-9593-00cd225e12ba	cc4fd81b-670a-45d2-9f30-960a15e5c938	synonym
a9c5b009-bb03-408f-9614-c032f7582322	1b20c43a-739a-4eae-9593-00cd225e12ba	7b9afe68-e7e2-45d5-8daa-b8b6922af786	compound
34dcea1d-5c1d-4e57-b823-f46b0f0e236c	1b20c43a-739a-4eae-9593-00cd225e12ba	9747b240-c1c0-49d5-9530-65851ccbe1cc	compound
38ccc3e7-8a2f-4971-99b4-02d255ce343d	1b20c43a-739a-4eae-9593-00cd225e12ba	765ff007-7986-4956-b484-dd0ebe106e57	kanji
9a506f8f-c366-480f-b6f6-7a873929d507	1b20c43a-739a-4eae-9593-00cd225e12ba	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	kanji
7f2d934b-f7aa-4c47-b010-70e50ef08dff	84fac07e-8c47-4136-beda-30bfb27cfb33	1039429c-3f56-482a-9d35-5e58a6f7dad7	synonym
f9acfe25-5f86-4675-b593-34c28fed422d	84fac07e-8c47-4136-beda-30bfb27cfb33	17f7f16a-8cf1-4b54-a1ff-5a5eadc98997	synonym
1b0bec77-0852-4da6-bd9e-ae7a7d45385e	84fac07e-8c47-4136-beda-30bfb27cfb33	2e8d78c7-afcd-43ad-8052-d560ac6de964	compound
8b8bfd0d-cee4-4707-bcfa-faae1d0a8452	84fac07e-8c47-4136-beda-30bfb27cfb33	2e35f1ed-6beb-4f20-ab03-e5ab3860ef49	compound
fe4f9f53-5bd7-4413-9be0-3ddaf7864730	84fac07e-8c47-4136-beda-30bfb27cfb33	0fc2835c-d402-402a-9c31-c6da37e86bce	kanji
a3c2a180-d366-42f5-b96c-c0b3c49ba7c9	84fac07e-8c47-4136-beda-30bfb27cfb33	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	kanji
b1789191-28cb-4e3d-9187-77837dd24be2	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	923494f5-4093-4866-9708-01e85b89b252	synonym
186ed6e4-f06d-4655-b03d-1911060a1470	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	eee468aa-47a6-4002-bb79-8f6a94902e57	antonym
1041504c-9ee8-472c-be1a-965f1e590828	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	aac210c6-0ef7-47c4-bcc3-ad3db49fbe07	compound
d228cb88-2b51-48fe-8aaf-494ddbf6e8c1	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	155926ec-47d3-4041-9a9d-3bb8389c1b20	kanji
f074ff0b-4e53-4e9a-a94e-08e48e073a7b	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	2855cf23-5e70-4042-8cbd-095389532ad7	synonym
65d58dc2-611e-4b7d-b8db-92d6a350c33d	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	6e32be34-6bb5-4b20-87f1-46a552cd23fa	synonym
895df7d1-a83a-45ea-a131-5db0d757ccf8	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	7c7c4f65-1b86-4bf7-aef6-d36dc16b7e9a	compound
d0db2be1-cba6-45e4-ba43-96f49d2c294a	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	4d7f0b57-6684-4318-bed8-9f7cb8090311	compound
a8c7873c-bde6-49dd-8ea7-7acef362a7e5	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	fd17d110-ae19-4d2e-b50f-e9283e5dd531	kanji
8224adc9-ee95-440f-9569-f782427dad28	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	71c68e5c-38d1-447b-841a-18df37edab6d	kanji
5082098d-c21d-42bc-89ec-906b3a0c43ee	bec461cd-95cc-4eec-99d0-201f1029484d	eae59957-4a4f-4e4c-9cee-723857e87658	synonym
38018278-3235-4d6f-8d30-1f849ad4a253	bec461cd-95cc-4eec-99d0-201f1029484d	f56b8d85-6fca-4e60-b157-4e29283c911b	synonym
05c112eb-6bce-4be0-96f8-0705385e0b29	bec461cd-95cc-4eec-99d0-201f1029484d	6126d949-ddbf-4bb6-bbfa-23c180c79a7b	antonym
2110da72-0b46-4a73-92b1-d384530a6786	bec461cd-95cc-4eec-99d0-201f1029484d	557cddd7-2b45-4415-96d2-5ffd262ff506	compound
01d7b4bc-2a61-47bc-881e-bdb622253639	bec461cd-95cc-4eec-99d0-201f1029484d	c8fc84e6-341f-47cf-b4af-308ff27e248c	compound
7e700e03-e9ae-4ef6-9711-4eadbdee47cb	bec461cd-95cc-4eec-99d0-201f1029484d	97504413-aa7f-487c-806f-55acf73f4756	kanji
9192ff01-c752-46fb-b8f1-aceb3db1b99f	bec461cd-95cc-4eec-99d0-201f1029484d	2853c152-be27-43f0-94cf-5ceb051202d9	kanji
00e44857-66c2-41ba-bf35-9d80c0ec4b50	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	e6b9e039-52de-4434-b07b-b90d59d84d1b	synonym
10549a17-046a-483a-8077-fae829bc7125	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	3c63318d-20a3-4dfd-97e1-377babed70c1	synonym
da0a0074-6e97-4981-ad79-9d17b79bd86f	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	460e2072-dddf-4ab7-927b-8d3338b27a6a	compound
a335d625-e190-4bdf-9646-3102c9f747a0	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	bfb86f51-659f-4ea6-8e55-e2750ae90d96	kanji
9d76f3f8-c886-44c7-813e-f50455312829	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	1550429a-13a2-4fed-8494-e88dd6c88ddd	kanji
46c01176-1f33-40cb-952e-a3c23d484254	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	1a30b075-876e-4204-bf12-a981bb7e86cd	compound
89b37a3b-408b-4936-a87f-3b5ef0aba15c	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	11fb167c-327d-45f1-9610-55c18c863ccb	compound
3fbcb0cf-ff34-445e-b7d6-add301d04c04	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	ac4120f9-c19a-44bf-9944-cc524196ad89	radical
03278f66-e27a-4263-b723-7af78d84c794	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	5d239c61-3559-4b41-9d1b-ee0bc64b5e93	radical
b46bc70a-62b6-4a74-b99e-470f1f78010d	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	9cbda39f-4123-4f36-8ef5-044ae0272394	compound
76ebeac7-d354-4da9-a63a-9d4cf24c3d78	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	67e04771-c3f5-400c-96d6-670d10fd628b	compound
08cbafc4-4725-4871-b030-f67c315228fa	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	266afaa2-2b4d-4afa-8b86-b9add9644bad	radical
761bf9a9-2def-485e-b9b8-800729fb48b1	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	a00fcc8e-3db5-405d-b182-1f612c25ee39	radical
1715c8bd-dffd-477c-8440-2a25b56f6784	995f94f1-08a5-4410-a982-1590fc0660af	1a30b075-876e-4204-bf12-a981bb7e86cd	compound
c631730f-86c3-4287-a2b2-e1b09e7923f9	995f94f1-08a5-4410-a982-1590fc0660af	8d6cc974-2d07-48dc-ad91-88ebcaad6fd0	compound
5f81f48e-e99a-4319-844c-a6c717cf35d4	995f94f1-08a5-4410-a982-1590fc0660af	26bbdc4f-bc47-4b2a-9272-530166f4251c	radical
5ff770eb-5fe9-4a07-b9a4-e3f89e4137a2	995f94f1-08a5-4410-a982-1590fc0660af	df25f971-029b-4dba-b8ba-83749c182888	radical
d0a460cc-3220-4b28-949e-4943e276dd37	af3d39b9-ebff-4062-a292-ff27b0028800	3782b95a-537a-4459-ba1e-9dc3308868d0	compound
e5cad77c-4cb6-471b-b926-072b47df7260	af3d39b9-ebff-4062-a292-ff27b0028800	bde111cd-aaa7-4e03-a754-9bb1fbce1e44	compound
d6f55f0e-64d3-4ff1-aca6-a7c2fb1df392	af3d39b9-ebff-4062-a292-ff27b0028800	c20a3f89-9451-4925-a845-661336d00311	radical
edab4bbe-c945-4eac-af15-c5f2c6b6a744	af3d39b9-ebff-4062-a292-ff27b0028800	bdf568f7-b39b-4c3f-a682-7b24c11c060e	radical
dfcbc446-b313-4327-91f6-b81bb995c411	af3d39b9-ebff-4062-a292-ff27b0028800	4c33a6c6-3b71-4839-92ab-f2b19afb3c1c	radical
59eff067-27f6-4aeb-9007-9dc9d069f8ba	af3d39b9-ebff-4062-a292-ff27b0028800	d07ec3f9-6cf2-4ebf-8f78-782d78f98c9d	radical
b690a049-fc41-4954-bc66-040f86a77dbb	fe926db8-7ef3-45a0-86be-6615013085b9	3782b95a-537a-4459-ba1e-9dc3308868d0	compound
3411b75e-aa69-4f20-b03e-555357ec3a44	fe926db8-7ef3-45a0-86be-6615013085b9	80b2739f-ce9a-487b-8e37-f3d850d2c55b	compound
95d2521d-d3cf-4ea2-93d5-8985efb38bf0	fe926db8-7ef3-45a0-86be-6615013085b9	f9df36b5-bdb1-4c97-9433-ea3678cb1c2e	radical
8629c223-9bc8-4ce9-bf4a-3ba6029578b3	fe926db8-7ef3-45a0-86be-6615013085b9	d7877559-6cc6-4a1a-ac00-0ce52d231ed3	radical
d5c07798-670b-42bf-87ae-630cd6705aac	319744e3-d340-4cbe-8b71-bd8796d65a94	252a4c0c-4a5d-490e-adc9-c004ad3f7694	compound
69e6a396-8d8f-45be-88c7-4c4ca45e15bf	319744e3-d340-4cbe-8b71-bd8796d65a94	c532f7c0-0075-4eec-ad39-869e0ee61317	compound
589fd01f-1d95-4080-8f9c-9e53bacff6c7	319744e3-d340-4cbe-8b71-bd8796d65a94	936c4b9e-a861-408f-b8f6-7ee7565932f5	radical
49ab34fa-8acc-4e92-b56c-689bf9b52686	319744e3-d340-4cbe-8b71-bd8796d65a94	331255cb-e956-4f76-8119-8290f0a24abc	radical
52666bd3-5ec3-41a2-bddf-a996ad8334b5	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	252a4c0c-4a5d-490e-adc9-c004ad3f7694	compound
3a0e49ce-1f05-4ef6-a8fb-3002292a34f4	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	f5669b9b-b90c-41af-b15f-436fe42ffe26	compound
bafc2b35-7eb9-450d-9388-5af99d8d27b6	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	4c33a6c6-3b71-4839-92ab-f2b19afb3c1c	radical
84394411-4f55-41e5-bff1-89d961f3854d	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	d600860f-5f21-4d7f-8994-d2cd5e9e4e70	radical
31df5f30-507a-434a-b427-bd1f369c255e	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	ff800669-8146-4efa-bf21-47b79692dd67	radical
0f1243eb-f90e-462b-9b0f-f405e2dd2f65	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	8c0f5eab-347d-4d5e-beb8-f899b5509b51	compound
82ade4b7-9d53-4e57-8068-39e075bf5ac8	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	d2e686e1-0bed-415b-adf9-c5d0137232e6	compound
cc2928fd-4516-430d-8d70-fc78396c9fd9	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	8e9be879-3d12-45e8-909f-584f18b2afe5	radical
899ac4c4-d3b8-4bfc-82f8-01a439c2efd3	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	f9835727-aad2-48bb-8edf-28a8bbf049ae	radical
2baef03a-deb3-4aa4-b3ec-dc04d9dfb3df	835da823-14ea-4221-a420-30288dda0e97	268f9c48-1f0f-49ac-a065-281e645009f1	compound
bf3eb11a-28cd-4a8f-9cc3-9c18c3fa83cd	835da823-14ea-4221-a420-30288dda0e97	3a9dc73a-22da-4c94-8b40-f07304c696d7	compound
d429a6d0-b313-42ed-af97-9caf3b623319	835da823-14ea-4221-a420-30288dda0e97	993038e4-a435-44a4-988a-777d16777ba6	radical
17876437-316c-4273-9e9f-5a47d23c8590	835da823-14ea-4221-a420-30288dda0e97	2dadcd7c-ba50-4157-b473-1c45c0ca5452	radical
9deda9cf-ee2b-4789-9029-d01f7248df96	2c02eb98-f34b-495e-a410-aef3f25fc414	268f9c48-1f0f-49ac-a065-281e645009f1	compound
e0012392-9ade-4410-934b-015b01cb7bed	2c02eb98-f34b-495e-a410-aef3f25fc414	9b814886-41dc-46ca-b14f-442ee814846f	compound
d831b09e-67df-4033-ad84-14c133443a6c	2c02eb98-f34b-495e-a410-aef3f25fc414	4eb3a489-ac3f-47ba-8f57-5148f55ef4c1	radical
8d6aeb18-2a0a-4bd7-8cda-f44470279b4d	2c02eb98-f34b-495e-a410-aef3f25fc414	42002556-d14d-403e-961b-7e735f0f3142	radical
a49f598c-d2eb-445c-bcbb-671fa97e0dbb	c16439b8-1685-449c-806f-cec26b1eec38	6474854c-a582-44d9-8e30-594f61d63b81	compound
fa947041-2919-4b09-a599-9a0a179141b5	c16439b8-1685-449c-806f-cec26b1eec38	1a63f90f-49e3-4442-a8c0-1e6b803cd990	compound
bf62a82d-954f-4111-9461-ce75b1964add	c16439b8-1685-449c-806f-cec26b1eec38	bec21e63-02dc-47ca-b50e-5893b9137884	radical
4b04ea8d-b2e1-495f-a17f-29b9dd607472	c16439b8-1685-449c-806f-cec26b1eec38	0081cd8a-c4ac-4d03-a15e-7183e98d4627	radical
a7c33d6a-28fb-45f4-a0cb-529c4d0c5a7c	6bee9a87-fe8d-42fe-a1a0-814844a2d157	b6d50bc3-2fbc-458a-8027-a34f72da42c1	compound
b744fcaa-8032-4276-a49f-ab5ca091004f	6bee9a87-fe8d-42fe-a1a0-814844a2d157	1f8aad77-1549-47ad-a892-993eb550e0ce	compound
1b700dcd-4a9f-4592-822c-bc5c662f72f1	6bee9a87-fe8d-42fe-a1a0-814844a2d157	744a896f-7075-4ac6-8753-33125aa9a337	radical
6b7aef87-2574-4c13-ad8a-c245929f9de2	6bee9a87-fe8d-42fe-a1a0-814844a2d157	92a18f8b-adc1-4016-b19d-719e89ffba3b	radical
eae0b2f5-7377-4647-a7e7-064a435ec316	b0d95268-5586-460e-9a28-a3cd08ef0a38	b6d50bc3-2fbc-458a-8027-a34f72da42c1	compound
00f61a1c-62f6-4781-a569-dc4d375dff67	b0d95268-5586-460e-9a28-a3cd08ef0a38	5a5cac90-9b4c-41e8-9c64-a36fd6b0a887	radical
0e0372d5-d3b2-41b4-aa4f-047bd67bbbb8	b0d95268-5586-460e-9a28-a3cd08ef0a38	9077cd16-e36b-4bf0-a1de-1bc64c22ff76	radical
82d2b338-f17d-4053-8faf-a9a39d935c0b	b0d95268-5586-460e-9a28-a3cd08ef0a38	9ad2abab-9a2c-4400-8298-270f1ca2f606	radical
a7ca700a-8045-4503-9e0f-b614120db758	8e8358b9-428c-40fe-9815-a4fcce537b8d	4c1a7005-1740-474d-a2de-1a06dd7b0027	compound
147ac309-982c-41e1-9e2c-4eca05e8fdd7	8e8358b9-428c-40fe-9815-a4fcce537b8d	c97423bb-d7e6-4940-a239-1d2c1f685327	compound
8a8ba909-9dd0-4c07-888f-e6ea2b2e0762	8e8358b9-428c-40fe-9815-a4fcce537b8d	a50f4072-38fa-4a32-834b-0a6672910e26	radical
35d0c827-f5d5-4a3e-9309-e034307b5cd1	8e8358b9-428c-40fe-9815-a4fcce537b8d	bc835f18-af16-4030-b21c-8a0c877b4b17	radical
8595548a-0589-401b-b372-fca4928e0fe9	7dd74620-3716-4d35-ab6b-c37049467492	46e7c181-4dba-402e-a903-6f249253080a	compound
bcf250f7-9414-48aa-b4d7-5103592784e2	7dd74620-3716-4d35-ab6b-c37049467492	b96aed4c-3149-45ac-aa32-d7c2f0f0e216	compound
aef920f0-f96f-4cc2-b1ec-6726264aa144	7dd74620-3716-4d35-ab6b-c37049467492	0e50ace3-335e-4332-a9c8-3010cd24bfff	radical
6e0354d4-2c2a-4bb3-996c-d28d694f74bd	7dd74620-3716-4d35-ab6b-c37049467492	46e8c4e2-7c15-4463-b4b3-c56f4ea4f10f	radical
d140c253-4d05-4c70-9727-abad255c6ab4	37ff6d9e-f8f0-49de-afdc-53763905bb15	c0656f9d-b3ec-4f2e-8763-7a25033d1157	compound
eae69e5b-fd31-46bc-ab83-51b2112dd8a0	37ff6d9e-f8f0-49de-afdc-53763905bb15	9977c060-7b9e-41ef-bc6b-d9d4b3b99aba	compound
7658eca8-dbbf-41c9-be83-d415a71e37e6	37ff6d9e-f8f0-49de-afdc-53763905bb15	a3182274-f724-4a5d-acb5-bd849bd5d1ae	radical
b17d76e4-ccf7-4063-a600-4396ec09d43d	37ff6d9e-f8f0-49de-afdc-53763905bb15	2620c4b9-7d6c-49fd-9aea-586f76bf5a08	radical
ad8ab73d-dfca-49e5-aef7-626dc088d106	5774f626-5ef3-45fa-85c9-03fdf53fd778	c0656f9d-b3ec-4f2e-8763-7a25033d1157	compound
c0fbd399-f8d6-45a0-a615-4066be7d00e3	5774f626-5ef3-45fa-85c9-03fdf53fd778	4f3c2689-b073-4316-9f56-c58e02563767	compound
d47f1304-b412-4a50-a398-1342dbf1fd4d	5774f626-5ef3-45fa-85c9-03fdf53fd778	60864f94-2107-4f7f-bcef-94a0f9e80b5c	radical
93b7c190-2de4-4840-97b4-acfab568e7f5	5774f626-5ef3-45fa-85c9-03fdf53fd778	fe13c5a5-ee85-4666-ac85-89aac99a7a9b	radical
f9a86175-e312-4dfc-ae22-8649a3bbe646	30da8d06-0ffc-459f-ab8a-0651fe92082b	54d3aeca-293e-4873-8c10-0122c492f361	compound
82108a0f-b5a2-43f3-bc9b-2da438607f7a	30da8d06-0ffc-459f-ab8a-0651fe92082b	74ff495b-9753-41ed-92c3-d81af032ea28	compound
b2aabb68-ab20-4d25-9c63-b1cce24317f9	30da8d06-0ffc-459f-ab8a-0651fe92082b	309ebd52-f82e-4111-bf46-2cb1862b95cd	radical
3c2bfbaa-38d7-400e-a0ed-ef542741dd29	30da8d06-0ffc-459f-ab8a-0651fe92082b	9077cd16-e36b-4bf0-a1de-1bc64c22ff76	radical
235b0cc9-5771-4c49-a589-25574fc604c4	1a30b075-876e-4204-bf12-a981bb7e86cd	88ddf1c4-499f-40b5-b8f5-723b6b76dbf3	synonym
6e724911-79cf-49f0-80c4-e097a867df41	1a30b075-876e-4204-bf12-a981bb7e86cd	93c5fa36-1387-44bf-875f-5f2ae40c5e92	compound
0bb1ca79-0fa1-4e16-bb89-76138fbe9cda	1a30b075-876e-4204-bf12-a981bb7e86cd	68be6074-44d5-4297-86d8-250d5760669a	compound
cc938896-d5cd-4bf9-a0fb-c5ac3c83c319	1a30b075-876e-4204-bf12-a981bb7e86cd	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	kanji
a776366e-c9b2-4935-a69a-6a00419115d4	1a30b075-876e-4204-bf12-a981bb7e86cd	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	kanji
6b326f09-d4ce-40f7-b5aa-ff0cc2524f1f	1a30b075-876e-4204-bf12-a981bb7e86cd	995f94f1-08a5-4410-a982-1590fc0660af	kanji
c294c7c3-b183-47dc-8449-a459fd6be1dc	3782b95a-537a-4459-ba1e-9dc3308868d0	cdad58fe-8a82-4421-8f2d-da6dc66c986e	synonym
1be4cd48-6c8a-4154-b23b-e8061b4df628	3782b95a-537a-4459-ba1e-9dc3308868d0	54c1e73a-22e9-4af0-8158-bb7ccfd16593	compound
9fd9b2da-bd40-4b51-a45f-b389b21b80da	3782b95a-537a-4459-ba1e-9dc3308868d0	0d1fe0f0-67f6-49bf-8274-1602f5242c1d	compound
60180748-6484-48cd-9c30-d90fbaac6f67	3782b95a-537a-4459-ba1e-9dc3308868d0	af3d39b9-ebff-4062-a292-ff27b0028800	kanji
e0e013d3-3a7a-4c78-946f-f2cc3fd30cac	3782b95a-537a-4459-ba1e-9dc3308868d0	fe926db8-7ef3-45a0-86be-6615013085b9	kanji
248ae776-ace3-4844-b3cc-18ab4f0ff678	252a4c0c-4a5d-490e-adc9-c004ad3f7694	4cf26c78-0567-4e62-ae2f-5df978a81b1d	synonym
d79c2040-e594-469a-8af9-9d8afde4f205	252a4c0c-4a5d-490e-adc9-c004ad3f7694	55c2e451-9bca-45ab-b9d5-cb154fed900c	synonym
af2b8770-eb20-4078-bba4-3f4b321d9058	252a4c0c-4a5d-490e-adc9-c004ad3f7694	ec694d9f-8b97-44d7-8414-33cf9bb50e1e	antonym
7ce5d3a6-df00-4c00-ae37-468eef9d5294	252a4c0c-4a5d-490e-adc9-c004ad3f7694	c45097bb-9122-447c-ab24-baec38948e69	antonym
b8855b09-fb29-47fd-9b74-500fe493c687	252a4c0c-4a5d-490e-adc9-c004ad3f7694	319744e3-d340-4cbe-8b71-bd8796d65a94	kanji
d21fd26b-bd3c-478c-bb29-f16f2acccfae	252a4c0c-4a5d-490e-adc9-c004ad3f7694	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	kanji
45bc331f-1e7a-4a60-b373-0b8c8ed390c8	8c0f5eab-347d-4d5e-beb8-f899b5509b51	57b67ad8-56b3-4b65-ada9-53edcd75533a	synonym
8d04551f-d187-43c9-a688-3fe3aa54e5b2	8c0f5eab-347d-4d5e-beb8-f899b5509b51	34e097b0-bee6-4593-a8d3-a58c162181e9	synonym
b14e4714-98bc-49a9-9577-fefcabdcf74a	8c0f5eab-347d-4d5e-beb8-f899b5509b51	54d3aeca-293e-4873-8c10-0122c492f361	antonym
8d2ae0b8-3de8-4e7f-8d21-f1e1780f8f2c	8c0f5eab-347d-4d5e-beb8-f899b5509b51	935008fd-bf1f-4a3d-a458-4f08ffda91f4	compound
4c5bbf08-0110-436a-afb4-1c9effe7d189	8c0f5eab-347d-4d5e-beb8-f899b5509b51	4f665837-e3fb-4652-a87a-99014b21b89d	compound
1a36b0a7-2def-48ec-9630-b638e5ac442a	8c0f5eab-347d-4d5e-beb8-f899b5509b51	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	kanji
b66d313f-c231-40a2-89be-09bc277f192c	268f9c48-1f0f-49ac-a065-281e645009f1	bbbe71e6-0a7c-47e9-bc60-6c1b8893b7ef	synonym
5564baf7-edf0-4531-b245-82febc13a7c8	268f9c48-1f0f-49ac-a065-281e645009f1	d9a2edb4-a02a-4001-a8b2-5140cd3837ad	synonym
aa7fa491-3b38-4213-8785-ba48a8d6903c	268f9c48-1f0f-49ac-a065-281e645009f1	2fd9212d-da95-4ede-812f-b34b7a76d28a	compound
0c8438f0-5b8c-47d7-84c5-060b9c1510b7	268f9c48-1f0f-49ac-a065-281e645009f1	c33f26bf-2ade-4f69-b5c8-2c936819f5c7	compound
2703bb16-960f-4fd7-8c2a-bf7f21f80122	268f9c48-1f0f-49ac-a065-281e645009f1	835da823-14ea-4221-a420-30288dda0e97	kanji
9e2e1494-fe20-4bc7-9cde-6d82c089052b	268f9c48-1f0f-49ac-a065-281e645009f1	2c02eb98-f34b-495e-a410-aef3f25fc414	kanji
354e84e6-0a7b-4969-ba54-dd1848eda0e3	6474854c-a582-44d9-8e30-594f61d63b81	b12c1e9f-36ba-450e-969b-c3f110b4d9ca	compound
706b3d1a-5b4f-465a-94b8-043eafe184cf	6474854c-a582-44d9-8e30-594f61d63b81	ad480e92-67ca-4f84-9bd9-bd2b430996fb	compound
1fbd0fa7-e884-44c0-b377-36f0e2853105	6474854c-a582-44d9-8e30-594f61d63b81	652cc60f-9a1e-4120-8ebd-1297daecea82	compound
441bdda6-a1e5-49ef-a167-71a53225a2ca	6474854c-a582-44d9-8e30-594f61d63b81	765ff007-7986-4956-b484-dd0ebe106e57	kanji
30fdec31-fd1c-44b0-832d-fadf7312571e	6474854c-a582-44d9-8e30-594f61d63b81	c16439b8-1685-449c-806f-cec26b1eec38	kanji
5dad64f2-5d52-4898-a2b9-34353ef723b9	b6d50bc3-2fbc-458a-8027-a34f72da42c1	11e23ac6-3e7e-47f1-abee-ab74205af51c	synonym
25966331-7242-4c46-beca-3aedec6313e2	b6d50bc3-2fbc-458a-8027-a34f72da42c1	9bcb2bd8-1a5f-418c-a9ec-99273510d4ac	synonym
9d950b97-da56-49de-bd76-3586a44cfe48	b6d50bc3-2fbc-458a-8027-a34f72da42c1	3ccef624-aec0-4097-9156-e06c7a0324c7	antonym
e844dcdc-787f-4af7-9d16-6ecfda3a1014	b6d50bc3-2fbc-458a-8027-a34f72da42c1	f2cbd464-a002-4571-891e-e8b26b0f43e1	antonym
6f8d4a94-f196-47a2-a697-84359611f268	b6d50bc3-2fbc-458a-8027-a34f72da42c1	483c7869-77ab-437d-87ae-2130fd26fb8a	compound
d2527612-df7d-4a5c-a4e4-e71031f833d7	b6d50bc3-2fbc-458a-8027-a34f72da42c1	6bee9a87-fe8d-42fe-a1a0-814844a2d157	kanji
c5b9d212-881e-4d5f-8917-33765ae9c56d	b6d50bc3-2fbc-458a-8027-a34f72da42c1	b0d95268-5586-460e-9a28-a3cd08ef0a38	kanji
c58306f7-5cfb-4c24-b075-4da9a9461a7f	46e7c181-4dba-402e-a903-6f249253080a	bf71c34a-7e17-44de-b076-b84a92a1e382	synonym
b8a70d8e-836b-4d26-b15e-dca7b74a6462	46e7c181-4dba-402e-a903-6f249253080a	9a5f75b6-0b25-403b-8b3f-8b0fa4eb7270	synonym
f7eca6c8-bee6-4055-a844-cc530bbb6e2d	46e7c181-4dba-402e-a903-6f249253080a	7e7b1c70-d795-4054-bcf4-de10aab9b1b8	compound
036c3ba3-c551-448e-bbee-3a9897e4b7bf	46e7c181-4dba-402e-a903-6f249253080a	2a99a105-8ce8-4691-9b81-61145f901796	compound
eea783ac-9a43-499f-b800-1d35bb3f91ca	46e7c181-4dba-402e-a903-6f249253080a	8e8358b9-428c-40fe-9815-a4fcce537b8d	kanji
7ca998a9-9492-4f5f-8952-6f8fd5a60649	46e7c181-4dba-402e-a903-6f249253080a	7dd74620-3716-4d35-ab6b-c37049467492	kanji
48b3e71f-4219-4384-825f-8a6541817852	c0656f9d-b3ec-4f2e-8763-7a25033d1157	20195b29-7239-4952-8a8e-5b17bf3b5cfb	synonym
ef03e676-5b77-43b6-a89e-eb62300ca0b6	c0656f9d-b3ec-4f2e-8763-7a25033d1157	d583902a-337f-4e3c-be5c-de5fc99d9a48	synonym
b97be016-0b16-4c6d-9a66-d28198e6e221	c0656f9d-b3ec-4f2e-8763-7a25033d1157	84b35d95-a556-4d6d-b011-04ab37c23cd5	compound
42e17014-383a-4fe7-ab5e-facc7d6f0424	c0656f9d-b3ec-4f2e-8763-7a25033d1157	9905f14d-4f83-4612-8721-cd3c5665dcb5	compound
c839fed1-041e-401e-b5fd-adee7600569a	c0656f9d-b3ec-4f2e-8763-7a25033d1157	37ff6d9e-f8f0-49de-afdc-53763905bb15	kanji
3385db34-092d-4515-8e9c-cef22b0e0c29	c0656f9d-b3ec-4f2e-8763-7a25033d1157	5774f626-5ef3-45fa-85c9-03fdf53fd778	kanji
1427c6b3-72a7-4e29-ace2-4646c5df5f36	54d3aeca-293e-4873-8c10-0122c492f361	03abe315-7883-4b31-a615-4fc8ab4fea46	synonym
5ea16760-2527-4cbf-93d9-a53a0529d3fd	54d3aeca-293e-4873-8c10-0122c492f361	8c0f5eab-347d-4d5e-beb8-f899b5509b51	antonym
f648c63a-64ab-451a-834d-75da0ee082bc	54d3aeca-293e-4873-8c10-0122c492f361	55283de8-eb01-4367-b8fe-ef74ff8d4759	antonym
ea0c6474-1bc9-40ee-bf77-f800141e68e2	54d3aeca-293e-4873-8c10-0122c492f361	74ff495b-9753-41ed-92c3-d81af032ea28	compound
fff27267-d2ec-4a2c-bd20-0869193e3cb0	54d3aeca-293e-4873-8c10-0122c492f361	ca20bc13-4746-44eb-bdfa-6c9fb3be5365	compound
b2947ede-b23f-4a3e-b7ca-7920f485fa70	54d3aeca-293e-4873-8c10-0122c492f361	30da8d06-0ffc-459f-ab8a-0651fe92082b	kanji
\.


--
-- Data for Name: examples; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.examples (id, entry_id, japanese_sentence, vietnamese_sentence) FROM stdin;
4dda0beb-8c33-4cef-a796-c58469732ffa	4b781e2f-06da-4af0-9ee1-db12231d67e5	ここに名前を書いてください。	Hãy viết tên vào đây.
92572bb4-8093-47ea-a34a-35a0e5a8648a	48768c0f-626c-4562-9e75-d8886d1042d7	富士山に登ったことがありますか。	Bạn đã từng leo núi Phú Sĩ chưa?
7efd666f-12da-46bf-9181-a812270ad422	187c683c-8c8d-4c0a-9059-64cd4f8814bb	薬を飲まなければなりません。	Tôi phải uống thuốc.
eacb507e-1638-4cdf-9e4b-8e63f9504ddb	cc54c6e3-12b6-4923-87fa-711b21cc8019	私は日本語を話すことができます。	Tôi có thể nói tiếng Nhật.
a180a036-aa2f-44ba-babc-044007a55505	43ec397a-37a2-433c-935e-40495fe27e27	早く寝たほうがいいですよ。	Bạn nên đi ngủ sớm thì tốt hơn.
4b53e59f-3206-450a-b6f8-5110cb2fcad5	bcbe9058-efdf-430b-960a-06b04e043201	来年、日本へ行くつもりです。	Sang năm tôi dự định sẽ đi Nhật.
ad31198a-4ba2-4046-bbdf-09f53247166e	6f2071da-85f7-4a3f-a0a0-0a09592fc172	この料理は辛すぎます。	Món ăn này cay quá mức.
25a0662e-ea83-4a6d-bb85-ac0785ffe8f4	4b36d5e8-0617-41af-bb0b-51b85e7a15a4	音楽を聞きながら勉強します。	Tôi vừa nghe nhạc vừa học bài.
186d502b-84c9-406c-ae5d-6172707c4b7f	f5af8af6-b458-48c2-9289-89611f584314	彼は今日来るはずです。	Anh ấy chắc chắn là sẽ đến hôm nay.
18436dce-fa99-4f9b-aaab-40e58e87db8c	d028bc1f-cc5f-4b55-a526-abb4950e5547	外は雨が降っているようです。	Hình như bên ngoài trời đang mưa.
0ab10a2d-c11f-4da1-9ee7-c7fc019637d2	74b7a2ad-edb0-4b22-bc9e-8874a277e517	健康のために毎日走っています。	Tôi chạy bộ mỗi ngày vì sức khỏe.
22402f06-9e82-4515-914d-6882c736fb4a	40d42513-b5ed-45b5-9d2b-62aa752cfaf1	一生懸命勉強したのに、不合格だった。	Mặc dù đã học hết mình vậy mà vẫn bị trượt.
34c18d47-b21c-42f9-bd19-d1e3e097e2b6	3ce52a73-530f-4218-a1bb-130869504339	忘れないうちにメモしておきます。	Tôi sẽ ghi chú lại trong khi chưa quên.
1b2c5b9c-417a-4f31-b0fd-30484e4d4376	b9791bc0-59fa-4f40-a0d5-22d5c8f3c7ae	先生のおかげで合格できました。	Nhờ có thầy giáo mà em đã thi đỗ.
9dbd4200-9586-4892-859a-020de08fd7d0	95ad4078-f7b0-4533-be8b-0db06ee4884b	さっきご飯を食べたばかりです。	Tôi vừa mới ăn cơm xong tức thì.
14d9542b-72aa-4cce-ac6f-c2af0e186c24	26ba816e-ab5f-4794-a86c-8445f2b9364d	人によって考え方が違います。	Tùy vào mỗi người mà cách suy nghĩ khác nhau.
b9bc55b9-2cb2-492d-b17d-62375030c9fb	faefb0eb-a835-4c6a-bee0-0b6188892387	説明書のとおりに組み立てました。	Tôi đã lắp ráp đúng như sách hướng dẫn.
b76227ab-89b9-4bdb-a888-a605b36639c2	9eb78070-2982-499d-867b-1f08dc38e377	わからなければ、先生に聞けばいいですよ。	Nếu không hiểu thì chỉ cần hỏi thầy là được mà.
e32554f3-8802-45ca-8955-712be16607eb	49bd1d8b-bd40-43d5-bd4b-2835bc05508d	病院へ行ったほうがいいですよ。	Bạn nên đi bệnh viện thì tốt hơn đấy.
e0ac25b5-8d37-4d8f-97e5-a21c7486abd9	db2e3cd8-b301-4108-b24e-6bae845d445d	5時までに帰らなければなりません。	Tôi phải về nhà trước 5 giờ.
e402a18d-2b03-4788-bea6-5099393bafd8	279dda32-0821-438e-8f35-4b873f615e3e	勉強	Học tập
03079ede-229f-4ea4-9436-b9d094d5a418	0777dc70-893b-42e1-966f-26e39f9e3739	強力	Mạnh mẽ/Lực lượng mạnh
19b4a872-ee8c-4584-a621-76403beb6113	df351a21-3695-4b50-9cbe-6149a03c1670	病気	Bệnh tật
74370466-f873-4538-a434-0613d473f3a7	d50eba17-e1cd-4b30-ba66-647824a1475d	大学院	Cao học
d02c7d64-e9f1-4f26-8d3b-6d976a0cd371	1d4f9c26-b3d3-494c-ac63-887c207b6406	困難	Khó khăn
a7e8a482-4516-482a-ab1d-bfd085b0ce08	47540271-a252-4fbe-8021-5496944d1e3f	基準	Tiêu chuẩn
1ca90144-b720-4e29-8c90-d8a42502c933	2f3c20b0-d8bf-4ece-a683-734faebaea30	設備	Thiết bị
e452dfcf-fbfb-4e5f-9125-89a20816f922	765ff007-7986-4956-b484-dd0ebe106e57	経済	Kinh tế
9e68dd12-4ab3-4cfa-a903-d301d0bccef7	865c4ac3-e6ba-4545-a2d0-44c0962e3a20	試験	Kỳ thi
4f3aa3a1-e520-4846-a4e3-4c45fcb391a4	0fc2835c-d402-402a-9c31-c6da37e86bce	連休	Kỳ nghỉ dài
5a23148e-512e-437d-8689-92a7482653c2	1ba0ef68-ba39-4321-b8aa-fa06ada48a5b	連絡	Liên lạc
fd115e83-4747-434e-803c-85c6261436f0	155926ec-47d3-4041-9a9d-3bb8389c1b20	多忙	Rất bận rộn
17244dbb-7828-4e11-ad69-bd9bc43c64ac	fd17d110-ae19-4d2e-b50f-e9283e5dd531	解決	Giải quyết
ab372c37-3f17-4d56-b3b8-5fafe8a3fac4	71c68e5c-38d1-447b-841a-18df37edab6d	決定	Quyết định
cf8c35a3-8487-4796-b25d-45cdda91d2a3	97504413-aa7f-487c-806f-55acf73f4756	興味	Hứng thú
0b6f2f86-0724-473d-9a3f-947d66be9cd0	2853c152-be27-43f0-94cf-5ceb051202d9	意味	Ý nghĩa
9afe899b-9936-45b8-8d32-90f5138beab3	bfb86f51-659f-4ea6-8e55-e2750ae90d96	申請	Thỉnh cầu/Đăng ký
2caed78b-b5a2-4dd6-b109-92fb3f8776b7	1550429a-13a2-4fed-8494-e88dd6c88ddd	屋上	Sân thượng
9447ce55-596e-401b-93d3-73e375e3916a	26bbdc4f-bc47-4b2a-9272-530166f4251c	食事	Bữa ăn
0534102a-6dbd-4d41-bbe9-706d31a0e50a	9de5a213-15c4-4cc4-836f-af2b0cf2bb26	仕事	Công việc
b7c0a54a-ed27-4d40-8b94-e1e3aeb4dd0c	3db95de6-b590-4eff-baf1-3e239689b322	毎日3時間日本語を勉強します。	Mỗi ngày tôi học tiếng Nhật 3 tiếng.
70feb8a1-8b3a-4ad5-a2b0-71eb014c8afa	07164d6b-941e-4f7b-ad9d-c80bacc93e8e	気分が悪いので、病院へ行きます。	Vì cảm thấy không khỏe nên tôi sẽ đi bệnh viện.
4829a83e-095a-4f19-9587-d8621c9a4cf0	11fcb009-8397-4770-a282-700a94910b07	この漢字の書き方はとても難しいです。	Cách viết chữ Kanji này rất khó.
b195849c-8f3e-4fc0-9041-69a35ccec808	2dacc031-c2ba-40b7-b0fb-8f8d05a37e8a	旅行の準備はもう終わりましたか？	Việc chuẩn bị cho chuyến du lịch đã xong chưa?
ee9a7410-b586-4340-bb98-af314c8daedb	1b20c43a-739a-4eae-9593-00cd225e12ba	日本で働いた経験があります。	Tôi có kinh nghiệm làm việc tại Nhật Bản.
0e2b4e09-4831-4695-856e-36bbe3777296	84fac07e-8c47-4136-beda-30bfb27cfb33	後でメールで連絡します。	Tôi sẽ liên lạc qua email sau.
db931730-0be7-4719-8ef7-e36d60d08550	0ed8d9c9-15ba-4f8d-be6e-341d412b42cb	今週は仕事がとても忙しいです。	Tuần này công việc rất bận rộn.
fb1a93db-c5c7-4b24-ab9a-6303d2e6a618	0fbfe50f-1c1d-48cc-98cd-34f03ac44e96	ようやく問題が解決しました。	Cuối cùng thì vấn đề đã được giải quyết.
4b5c72a0-8f92-4043-915b-e91e482839ab	bec461cd-95cc-4eec-99d0-201f1029484d	私は日本の歴史に興味があります。	Tôi có hứng thú với lịch sử Nhật Bản.
cd413f6e-795c-4d0e-ac46-64dc8f451777	3eea0ea6-1ff6-49ee-bb02-cc0780f4cf5f	お礼申し上げます。	Tôi xin được bày tỏ lòng cảm ơn.
816f6d4e-56c2-4c07-8aab-725e9e421ed1	d11c9ef7-1f3d-4221-b7db-ffa9203a3524	地図	Bản đồ
ad12afc0-6ef3-4c60-90d3-ab5bab63a5de	6198b623-c6ec-4d23-a58c-546fa3ddfe3e	教科書	Sách giáo khoa
d0ddc59f-56e2-483b-bf02-51503bc9e937	995f94f1-08a5-4410-a982-1590fc0660af	映画館	Rạp chiếu phim
67dc673a-6f2f-428b-ae5e-8d6d74dbbac0	af3d39b9-ebff-4062-a292-ff27b0028800	解散	Giải tán
f563e22d-d43d-4a87-b56b-51ff9d30d54f	fe926db8-7ef3-45a0-86be-6615013085b9	歩行者	Người đi bộ
75c4e367-e81e-4922-8202-61fb0cfc8eb9	319744e3-d340-4cbe-8b71-bd8796d65a94	安全	An toàn
ec06be57-f260-4245-aca5-67f0dfbe086e	9a036d0f-47f6-40ff-92c1-fb6f70cacf8b	自然	Tự nhiên
0084b80f-ca1f-421c-9459-d902a9510688	ea72fd2e-79f6-4b9a-bee1-2f6fe65621fd	感覚	Cảm giác
e7036c22-b77c-46f3-9a02-0d25d01f5f0c	835da823-14ea-4221-a420-30288dda0e97	相手	Đối phương
dc50d5d8-9b95-44a4-bc4a-141b6bd199b5	2c02eb98-f34b-495e-a410-aef3f25fc414	冗談	Nói đùa
a8cf0fd2-cb1d-4e8e-b87d-9b24c1ca0ae1	c16439b8-1685-449c-806f-cec26b1eec38	済む	Kết thúc/Xong xuôi
1a233a72-4cc5-4db1-8f34-7c17675d8891	6bee9a87-fe8d-42fe-a1a0-814844a2d157	丁寧	Lịch sự/Cẩn thận
767a1dcb-7e3b-4bb0-83ee-71adcc9384fe	b0d95268-5586-460e-9a28-a3cd08ef0a38	安寧	An ninh/Yên ổn
042d4d11-47ba-485f-9364-9cbc69ce1fb0	8e8358b9-428c-40fe-9815-a4fcce537b8d	習慣	Thói quen
c3cb8e23-4021-4426-b941-c8bfbc0ba07c	7dd74620-3716-4d35-ab6b-c37049467492	慣習	Tập quán
c3ab879d-11ac-4315-8471-c6b69d25a3a0	37ff6d9e-f8f0-49de-afdc-53763905bb15	出発	Xuất phát
b44c52cf-cb54-4eb8-a605-d4fd938aa623	5774f626-5ef3-45fa-85c9-03fdf53fd778	代表	Đại diện
152d6b93-88f1-4cd1-8696-b205be218631	30da8d06-0ffc-459f-ab8a-0651fe92082b	忘年会	Tiệc tất niên (tiệc quên năm cũ)
54410f96-98e2-484e-88ef-25b2b5205db1	1a30b075-876e-4204-bf12-a981bb7e86cd	図書館で静かに勉強します。	Tôi học bài yên tĩnh ở thư viện.
26af2c06-55b4-4af6-ae2d-58f1278701d0	3782b95a-537a-4459-ba1e-9dc3308868d0	公園を散歩するのが好きです。	Tôi thích đi dạo ở công viên.
760bd413-d192-4e07-af4e-e37747aa209e	252a4c0c-4a5d-490e-adc9-c004ad3f7694	昨日のテストは全然わかりませんでした。	Bài kiểm tra hôm qua tôi hoàn toàn không hiểu gì cả.
03f8d4e0-e2b5-40b7-9035-e6992423b295	8c0f5eab-347d-4d5e-beb8-f899b5509b51	新しい単語を10個覚えました。	Tôi đã thuộc lòng 10 từ mới.
39b6ade0-aac3-476e-9871-66eb73349702	268f9c48-1f0f-49ac-a065-281e645009f1	進路について先生に相談しました。	Tôi đã thảo luận với thầy giáo về con đường tương lai.
506b45f6-add2-402e-9fd5-2aac57d9627b	6474854c-a582-44d9-8e30-594f61d63b81	日本の経済について論文を書きます。	Tôi sẽ viết luận văn về kinh tế Nhật Bản.
3f83ec4c-1400-4ffe-9bc1-291cab9e0589	b6d50bc3-2fbc-458a-8027-a34f72da42c1	手紙を丁寧な字で書きました。	Tôi đã viết thư bằng chữ viết cẩn thận.
13483223-c0ca-47be-b17c-8410fe8a0451	46e7c181-4dba-402e-a903-6f249253080a	早寝早起きは良い習慣です。	Ngủ sớm dậy sớm là một thói quen tốt.
2b34be76-369e-472e-9b00-b9015b4d0a6b	c0656f9d-b3ec-4f2e-8763-7a25033d1157	来週、クラスで研究の結果を発表します。	Tuần tới, tôi sẽ thuyết trình kết quả nghiên cứu trước lớp.
3e14581b-c96c-49ca-a250-92371678881c	54d3aeca-293e-4873-8c10-0122c492f361	宿題を家に忘れてしまいました。	Tôi lỡ quên bài tập ở nhà mất rồi.
\.


--
-- Data for Name: flashcards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flashcards (id, user_id, entry_id, source, status, created_at, ease_factor, "interval", next_review_at, repetitions) FROM stdin;
\.


--
-- Data for Name: learning_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.learning_logs (id, session_id, role, content, created_at) FROM stdin;
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.refresh_tokens (id, expiry_date, token, user_id) FROM stdin;
\.


--
-- Data for Name: srs_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.srs_details (flashcard_id, ease_factor, interval_days, repetitions, next_review) FROM stdin;
\.


--
-- Data for Name: user_profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_profiles (user_id, daily_goal_minutes, streak_count, total_points, last_active) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, email, password_hash, target_level, created_at, password, role) FROM stdin;
\.


--
-- Name: dictionary_entries_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dictionary_entries_seq', 1, false);


--
-- Name: app_refresh_tokens app_refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_refresh_tokens
    ADD CONSTRAINT app_refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: app_users app_users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT app_users_pkey PRIMARY KEY (id);


--
-- Name: comments comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: conversation_sessions conversation_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversation_sessions
    ADD CONSTRAINT conversation_sessions_pkey PRIMARY KEY (id);


--
-- Name: detected_errors detected_errors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detected_errors
    ADD CONSTRAINT detected_errors_pkey PRIMARY KEY (id);


--
-- Name: dictionary_entries dictionary_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dictionary_entries
    ADD CONSTRAINT dictionary_entries_pkey PRIMARY KEY (id);


--
-- Name: entry_relations entry_relations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entry_relations
    ADD CONSTRAINT entry_relations_pkey PRIMARY KEY (id);


--
-- Name: examples examples_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.examples
    ADD CONSTRAINT examples_pkey PRIMARY KEY (id);


--
-- Name: flashcards flashcards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flashcards
    ADD CONSTRAINT flashcards_pkey PRIMARY KEY (id);


--
-- Name: learning_logs learning_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.learning_logs
    ADD CONSTRAINT learning_logs_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: srs_details srs_details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.srs_details
    ADD CONSTRAINT srs_details_pkey PRIMARY KEY (flashcard_id);


--
-- Name: app_users uk4vj92ux8a2eehds1mdvmks473; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT uk4vj92ux8a2eehds1mdvmks473 UNIQUE (email);


--
-- Name: app_refresh_tokens uk54q0fvjjt1viugx3venqm9gdn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_refresh_tokens
    ADD CONSTRAINT uk54q0fvjjt1viugx3venqm9gdn UNIQUE (token);


--
-- Name: refresh_tokens ukghpmfn23vmxfu3spu3lfg4r2d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT ukghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token);


--
-- Name: flashcards uq_flashcard_user_entry; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flashcards
    ADD CONSTRAINT uq_flashcard_user_entry UNIQUE (user_id, entry_id);


--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (user_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_dictionary_text; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dictionary_text ON public.dictionary_entries USING btree (text);


--
-- Name: idx_flashcards_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_flashcards_user ON public.flashcards USING btree (user_id);


--
-- Name: idx_srs_next_review; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_srs_next_review ON public.srs_details USING btree (next_review);


--
-- Name: comments comments_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_entry_id_fkey FOREIGN KEY (entry_id) REFERENCES public.dictionary_entries(id) ON DELETE CASCADE;


--
-- Name: comments comments_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.comments(id) ON DELETE CASCADE;


--
-- Name: comments comments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: conversation_sessions conversation_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversation_sessions
    ADD CONSTRAINT conversation_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: detected_errors detected_errors_log_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detected_errors
    ADD CONSTRAINT detected_errors_log_id_fkey FOREIGN KEY (log_id) REFERENCES public.learning_logs(id) ON DELETE CASCADE;


--
-- Name: entry_relations entry_relations_source_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entry_relations
    ADD CONSTRAINT entry_relations_source_id_fkey FOREIGN KEY (source_id) REFERENCES public.dictionary_entries(id) ON DELETE CASCADE;


--
-- Name: entry_relations entry_relations_target_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entry_relations
    ADD CONSTRAINT entry_relations_target_id_fkey FOREIGN KEY (target_id) REFERENCES public.dictionary_entries(id) ON DELETE CASCADE;


--
-- Name: examples examples_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.examples
    ADD CONSTRAINT examples_entry_id_fkey FOREIGN KEY (entry_id) REFERENCES public.dictionary_entries(id) ON DELETE CASCADE;


--
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: flashcards fkc3opc7fxelc82rp32gof2irxp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flashcards
    ADD CONSTRAINT fkc3opc7fxelc82rp32gof2irxp FOREIGN KEY (user_id) REFERENCES public.app_users(id);


--
-- Name: app_refresh_tokens fkoyi0jlrmf1dwr978ifcrcvs88; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_refresh_tokens
    ADD CONSTRAINT fkoyi0jlrmf1dwr978ifcrcvs88 FOREIGN KEY (user_id) REFERENCES public.app_users(id);


--
-- Name: flashcards flashcards_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flashcards
    ADD CONSTRAINT flashcards_entry_id_fkey FOREIGN KEY (entry_id) REFERENCES public.dictionary_entries(id) ON DELETE CASCADE;


--
-- Name: learning_logs learning_logs_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.learning_logs
    ADD CONSTRAINT learning_logs_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.conversation_sessions(id) ON DELETE CASCADE;


--
-- Name: srs_details srs_details_flashcard_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.srs_details
    ADD CONSTRAINT srs_details_flashcard_id_fkey FOREIGN KEY (flashcard_id) REFERENCES public.flashcards(id) ON DELETE CASCADE;


--
-- Name: user_profiles user_profiles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict hwtMw4rsjG2WGRgyiMglhubm9RNMohxK8tkY1ydvc8XKgh3pZdR49cxXCp19W1w

