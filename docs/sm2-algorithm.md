# Thuật Toán SM2 (SuperMemo 2)

> **SM2** là thuật toán lập lịch ôn tập dựa trên *Spaced Repetition* (lặp lại có giãn cách), được phát triển bởi **Piotr Woźniak** vào năm 1987. Đây là nền tảng của ứng dụng SuperMemo và được sử dụng rộng rãi trong các phần mềm học ngôn ngữ như Anki.

---

## 1. Bài Toán Đặt Ra

Khi học một lượng lớn từ vựng, não bộ có xu hướng **quên theo đường cong quên lãng (Forgetting Curve)** của Hermann Ebbinghaus:

- Sau **20 phút**: quên ~42% thông tin
- Sau **1 giờ**: quên ~56%
- Sau **1 ngày**: quên ~67%
- Sau **1 tuần**: quên ~75%

**Giải pháp**: Ôn lại thông tin **đúng thời điểm** — ngay trước khi não sắp quên — để củng cố ký ức dài hạn với số lần ôn ít nhất có thể.

---

## 2. Các Khái Niệm Cốt Lõi

| Ký hiệu | Tên | Mô tả |
|---------|-----|-------|
| `E-Factor` (EF) | Easiness Factor | Hệ số dễ dàng, đo mức độ "dễ nhớ" của thẻ. Giá trị trong `[1.3, 2.5]`, mặc định `2.5` |
| `n` | Repetition Number | Số lần ôn thành công liên tiếp |
| `I(n)` | Interval | Khoảng thời gian (ngày) cho đến lần ôn tiếp theo |
| `q` | Quality | Chất lượng phản hồi của người học, thang điểm `0–5` |

### Thang điểm chất lượng `q`

| `q` | Ý nghĩa |
|-----|---------|
| **5** | Trả lời hoàn hảo, không do dự |
| **4** | Trả lời đúng, sau một chút suy nghĩ |
| **3** | Trả lời đúng, nhưng khó khăn đáng kể |
| **2** | Trả lời sai, nhưng khi thấy đáp án thì nhớ ra ngay |
| **1** | Trả lời sai, nhưng đáp án có vẻ quen |
| **0** | Hoàn toàn không nhớ |

> **Quy tắc ngón tay cái**: `q ≥ 3` là ôn thành công; `q < 3` cần ôn lại ngay.

---

## 3. Công Thức SM2

### 3.1 Tính Interval `I(n)`

```
Nếu n = 1:  I(1) = 1 (ngày)
Nếu n = 2:  I(2) = 6 (ngày)
Nếu n > 2:  I(n) = I(n-1) × EF
```

### 3.2 Cập Nhật E-Factor

Sau mỗi lần ôn, `EF` được cập nhật theo công thức:

```
EF' = EF + (0.1 - (5 - q) × (0.08 + (5 - q) × 0.02))
```

Ràng buộc: `EF' = max(1.3, EF')`  
*(EF không bao giờ nhỏ hơn 1.3)*

### 3.3 Khai Triển Công Thức EF

Với mỗi giá trị `q`, phần thay đổi `Δ(q) = 0.1 - (5 - q) × (0.08 + (5 - q) × 0.02)`:

| `q` | `Δ(q)` | Ý nghĩa |
|-----|--------|---------|
| 5 | +0.10 | EF tăng mạnh |
| 4 | +0.10 | EF giữ nguyên (≈) |
| 3 | -0.14 | EF giảm nhẹ |
| 2 | -0.32 | EF giảm đáng kể |
| 1 | -0.54 | EF giảm mạnh |
| 0 | -0.80 | EF giảm rất mạnh |

---

## 4. Luồng Thuật Toán

```
┌─────────────────────────────────────────────────────────┐
│                   Người học xem thẻ                     │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │  Nhập chất lượng q    │
              │  (thang điểm 0 – 5)   │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │       q ≥ 3?          │
              └───────┬───────┬───────┘
                   Có │       │ Không
                      │       │
          ┌───────────▼─┐   ┌─▼──────────────────────┐
          │  Cập nhật   │   │  Reset: n = 0, I = 1    │
          │  EF theo q  │   │  Xếp thẻ vào hàng đợi  │
          │  Tăng n lên │   │  ôn lại trong phiên     │
          │  Tính I(n)  │   └────────────────────────┘
          └──────┬──────┘
                 │
          ┌──────▼──────────────────────┐
          │  Lên lịch lần ôn tiếp theo  │
          │  = hôm nay + I(n) ngày      │
          └─────────────────────────────┘
```

---

## 5. Pseudocode

```python
def sm2_review(card, q):
    """
    card: object chứa {ef, n, interval, next_review}
    q   : chất lượng phản hồi (0-5)
    """
    # Bước 1: Cập nhật E-Factor
    new_ef = card.ef + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
    card.ef = max(1.3, new_ef)

    # Bước 2: Xử lý theo kết quả
    if q >= 3:
        # Ôn thành công
        if card.n == 0:
            card.interval = 1
        elif card.n == 1:
            card.interval = 6
        else:
            card.interval = round(card.interval * card.ef)
        card.n += 1
    else:
        # Ôn thất bại → reset
        card.n = 0
        card.interval = 1

    # Bước 3: Cập nhật ngày ôn tiếp theo
    card.next_review = today() + card.interval (days)

    return card
```

---

## 6. Ví Dụ Minh Họa

Giả sử học một thẻ từ vựng mới với `EF = 2.5`, `n = 0`, `I = 0`:

| Lần ôn | `q` | `n` trước | `I` (ngày) | Ngày ôn tiếp | `EF` mới |
|--------|-----|-----------|------------|--------------|----------|
| 1      | 4   | 0 → 1     | 1          | Ngày 2       | 2.50     |
| 2      | 5   | 1 → 2     | 6          | Ngày 8       | 2.60     |
| 3      | 3   | 2 → 3     | 15 (6×2.6) | Ngày 23      | 2.46     |
| 4      | 4   | 3 → 4     | 37 (15×2.46)| Ngày 60    | 2.46     |
| 5      | 2   | 4 → 0     | 1 (reset)  | Ngày 61      | 2.14     |
| 6      | 4   | 0 → 1     | 1          | Ngày 62      | 2.14     |

---

## 7. Cài Đặt Java

Dưới đây là ví dụ triển khai SM2 trong Java cho dự án Spring Boot:

```java
import java.time.LocalDate;

public class SM2Algorithm {

    private static final double MIN_EF = 1.3;
    private static final double INITIAL_EF = 2.5;

    /**
     * Xử lý kết quả ôn tập và trả về trạng thái thẻ mới.
     *
     * @param ef       E-Factor hiện tại
     * @param n        Số lần ôn thành công liên tiếp
     * @param interval Khoảng cách ôn hiện tại (ngày)
     * @param quality  Chất lượng phản hồi (0–5)
     * @return ReviewResult chứa EF, n, interval, và nextReviewDate mới
     */
    public static ReviewResult calculate(double ef, int n, int interval, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality phải trong khoảng [0, 5]");
        }

        // Cập nhật E-Factor
        double newEf = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        newEf = Math.max(MIN_EF, newEf);

        int newN;
        int newInterval;

        if (quality >= 3) {
            // Ôn thành công
            newInterval = switch (n) {
                case 0 -> 1;
                case 1 -> 6;
                default -> (int) Math.round(interval * newEf);
            };
            newN = n + 1;
        } else {
            // Ôn thất bại → reset
            newN = 0;
            newInterval = 1;
        }

        LocalDate nextReview = LocalDate.now().plusDays(newInterval);

        return new ReviewResult(newEf, newN, newInterval, nextReview);
    }

    public record ReviewResult(
        double ef,
        int repetitionNumber,
        int interval,
        LocalDate nextReviewDate
    ) {}
}
```

### Tích hợp với Entity `Flashcard`

```java
// Trong FlashcardServiceImpl.java
public FlashcardResponse submitReview(UUID flashcardId, ReviewRequest request) {
    Flashcard card = flashcardRepository.findById(flashcardId)
        .orElseThrow(() -> new NotFoundException("Flashcard not found"));

    SrsDetail srs = srsDetailRepository.findByFlashcard(card).orElseGet(() -> createDefault(card));

    SM2Algorithm.ReviewResult result = SM2Algorithm.calculate(
        srs.getEaseFactor(),
        srs.getRepetitions(),
        srs.getIntervalDays(),
        request.getRating().toQuality()   // AGAIN=1, HARD=3, GOOD=4, EASY=5
    );

    srs.setEaseFactor(result.easeFactor());
    srs.setRepetitions(result.repetitions());
    srs.setIntervalDays(result.intervalDays());
    srs.setNextReview(result.nextReview());
    srsDetailRepository.save(srs);

    // Cập nhật status dựa trên số lần ôn thành công liên tiếp
    // • repetitions == 0 → reset (thất bại) → LEARNING
    // • repetitions == 1 → lần đầu được, interval 1 ngày → LEARNING
    // • repetitions >= 2 → tốt nghiệp vào hàng đợi ôn → REVIEW
    FlashcardStatus newStatus = (result.repetitions() >= 2)
        ? FlashcardStatus.REVIEW
        : FlashcardStatus.LEARNING;

    card.setStatus(newStatus);
    flashcardRepository.save(card);
    return flashcardMapper.toResponse(card);
}
```

---

## 8. Các Cải Tiến Phổ Biến

### 8.1 Fuzz Factor (Anki)
Anki thêm một hệ số ngẫu nhiên nhỏ vào `interval` để tránh nhiều thẻ bị lên lịch cùng một ngày:
```
interval_fuzzy = interval × random(0.95, 1.05)
```

### 8.2 Hard / Good / Easy (Anki)
Anki mở rộng thang điểm thành 4 nút bấm thay vì nhập số:

| Nút | Tương đương `q` | Mô tả |
|-----|----------------|-------|
| Again | 1 | Quên hoàn toàn |
| Hard | 3 | Nhớ được nhưng khó |
| Good | 4 | Nhớ bình thường |
| Easy | 5 | Nhớ rất dễ |

### 8.3 FlashcardStatus Lifecycle (dự án này)

Dự án dùng 3 trạng thái được thiết kế theo mô hình Anki:

| Status | DB/API value | Ý nghĩa | Khi nào xảy ra |
|--------|-------------|----------|----------------|
| `new` | `"new"` | Thẻ mới tạo, chưa được ôn lần nào | Khi `POST /flashcards` |
| `learning` | `"learning"` | Đang học tích cực (interval ngắn) | Sau lần review đầu tiên, hoặc sau khi thất bại (reset) |
| `review` | `"review"` | Đã tốt nghiệp, đang ôn theo SRS | Sau `repetitions >= 2` (interval >= 6 ngày) |

**Chuyển trạng thái:**
```
new ──(bất kỳ review)──► learning ──(repetitions ≥ 2)──► review
                             ▲                                  │
                             └────────(rating = again)──────────┘
```

**Lưu ý kỹ thuật:** Java keyword `new` không thể làm tên enum constant.
Java enum sử dụng `NEW_CARD` nhưng serialize/store thành `"new"` qua
`FlashcardStatusConverter` (JPA `AttributeConverter`) và `@JsonValue`.

### 8.4 Graduation Steps (New Cards)
Thẻ mới thường qua các bước học ban đầu (ví dụ: 1 phút → 10 phút → 1 ngày) trước khi được đưa vào lịch SM2 chính thức.

---

## 9. Ưu & Nhược Điểm

### ✅ Ưu điểm
- **Đơn giản**: Dễ hdiểu, dễ cài đặt
- **Hiệu quả**: Giảm số lần ôn d thiết ~60–70% so với học truyền thống
- **Cá nhân hóa**: EF tự động điều chỉnh theo từng người học và từng thẻ

### ❌ Nhược điểm
- **Không xét ngữ cảnh**: Không phân biệt được thẻ mới học so với thẻ đã biết từ lâu
- **Ứng suất buổi học**: Nhiều thẻ đến hạn cùng lúc sau kỳ nghỉ
- **Chủ quan**: Người học tự đánh giá `q` → dễ bias
- **Phiên bản cũ**: SM2 đã được thay thế bởi SM-17/SM-18 trong SuperMemo hiện đại, nhưng vẫn là chuẩn mực trong cộng đồng mã nguồn mở

---

## 10. Tài Liệu Tham Khảo

- [SuperMemo Algorithm SM-2](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2) — Piotr Woźniak, 1990
- [Ebbinghaus Forgetting Curve](https://en.wikipedia.org/wiki/Forgetting_curve) — Wikipedia
- [Anki's scheduling algorithm](https://faqs.ankiweb.net/what-spaced-repetition-algorithm.html) — Anki Docs
- [Spaced Repetition](https://en.wikipedia.org/wiki/Spaced_repetition) — Wikipedia

---

*Tài liệu này được soạn cho dự án **Smart Japanese Learning System** — DATN 2026.*
