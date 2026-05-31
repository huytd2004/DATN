# AI Tutor (DATN)

Dự án AI Tutor là một hệ thống học tập gồm nhiều thành phần: backend Java (Spring/Maven), frontend (Vite + Vue), và các mô-đun AI/ML bằng Python (trong `ai/langraph_pipeline`). Mục tiêu: cung cấp trải nghiệm dạy/kéo luyện ngôn ngữ với nội dung, flashcards, và hỗ trợ giọng nói.

**Key Components**
- **ai/langraph_pipeline**: Pipeline xử lý ngôn ngữ, client LLM, TTS/STT, và các script chạy pipeline.
- **backend**: API server (Spring Boot) xử lý logic nghiệp vụ, DB, và endpoints cho frontend.
- **frontend**: Giao diện người dùng (Vite + Vue).
- **neo4j-data**: Dữ liệu CSV dùng để khởi tạo đồ thị Neo4j.
- **uploads/tutor-audio**: Thư mục chứa file audio cho bài tập và mẫu.

**Key Features**
- **Tích hợp SM-2 (Spaced Repetition)**: Hệ thống dùng thuật toán SM-2 để lên lịch ôn tập flashcards và từ vựng theo khoảng cách lặp lại tối ưu, hỗ trợ lưu trạng thái lịch sử học viên và điều chỉnh độ khó tự động.
- **Dịch chuyên ngành dựa trên graph + LLM**: Sử dụng đồ thị ngữ nghĩa (Neo4j) để trích xuất đúng nghĩa (sense disambiguation) của thuật ngữ chuyên ngành, sau đó kết hợp với LLM để sinh bản dịch chính xác và bối cảnh phù hợp.
- **Học và luyện từ vựng bằng giọng nói với phản hồi AI**: Hỗ trợ TTS/STT để người dùng luyện nói, nhận phản hồi bằng văn bản/âm thanh, đánh giá lỗi phát âm, ngữ pháp hoặc ngữ nghĩa và gợi ý sửa lỗi.
- **Ôn tập lỗi và deck qua bài tập/câu chuyện do AI tạo**: Tự động sinh bài tập, câu hỏi hoặc câu chuyện dựa trên những lỗi thường gặp của người học; cập nhật deck ôn tập dựa trên hiệu suất thực tế.

**Prerequisites**
- Java 17+ and Maven (project có kèm `mvnw`).
- Node.js 18+ and npm/yarn.
- Python 3.10+ (hoặc môi trường tương thích) để chạy các mô-đun trong `ai/`.
- (Tùy chọn) Neo4j hoặc PostgreSQL nếu bạn cần import dữ liệu (xem scripts trong `neo4j-data`/`crawl-data`).

**Quickstart — Backend**
1. Từ gốc repo, build và chạy Spring Boot:

```bash
./mvnw -f backend/pom.xml spring-boot:run
```

2. Cấu hình: kiểm tra `backend/src/main/resources` để điều chỉnh `application.yml` hoặc biến môi trường.

**Quickstart — AI pipeline (Python)**
1. Vào thư mục AI và tạo virtualenv:

```bash
cd ai/langraph_pipeline
python3 -m venv env
source env/bin/activate
pip install -r requirements.txt
```

2. Chạy server/pipeline (phát triển):

```bash
python server.py
# hoặc
python runner.py
```

Xem thêm: [ai/langraph_pipeline/README.md](ai/langraph_pipeline/README.md#L1)

**Quickstart — Frontend**

```bash
cd frontend
npm install
npm run dev
```

Frontend mặc định chạy trên `http://localhost:5173` (Vite) — cấu hình có thể thay đổi trong `vite.config.js`.

**Database & Data Imports**
- Các file dữ liệu mẫu và script import nằm ở `neo4j-data/` và `crawl-data/`. Kiểm tra `docs/` để biết sơ đồ và lược đồ dữ liệu.

**Testing**
- Backend tests: `mvn -f backend/pom.xml test`
- Frontend: kiểm tra `frontend/package.json` scripts (có thể có `test`/`cypress`).

**Development notes**
- Giữ environment của Python (trong `ai/`) tách biệt với backend Java.
- Các API contract nằm trong `docs/tutor-rest-contract.md`.

**Contributing**
- Fork → tạo branch → PR. Ghi rõ mô tả thay đổi và cách chạy (migration, seed data nếu cần).

**License & Contact**
- Kiểm tra file license trong repo (nếu có). Liên hệ tác giả hoặc nhóm dự án trong thông tin repository.

---

Nếu bạn muốn, tôi có thể mở rộng phần "Cấu hình môi trường" chi tiết hơn cho từng thành phần, hoặc thêm hướng dẫn chạy demo end-to-end.
