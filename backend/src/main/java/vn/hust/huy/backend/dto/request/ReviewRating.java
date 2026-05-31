package vn.hust.huy.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Anki-style review rating that maps to SM-2 quality scores (0–5).
 *
 * <p>Reference: sm2-algorithm.md §8.2 — Hard/Good/Easy model
 *
 * <table>
 *   <tr><th>Rating</th><th>SM-2 quality (q)</th><th>Meaning</th></tr>
 *   <tr><td>AGAIN</td><td>1</td><td>Complete blackout / forgot</td></tr>
 *   <tr><td>HARD</td> <td>3</td><td>Remembered with difficulty</td></tr>
 *   <tr><td>GOOD</td> <td>4</td><td>Remembered after slight hesitation</td></tr>
 *   <tr><td>EASY</td> <td>5</td><td>Perfect recall</td></tr>
 * </table>
 */
public enum ReviewRating {

    AGAIN(1),
    HARD(3),
    GOOD(4),
    EASY(5);

    private final int quality;

    ReviewRating(int quality) {
        this.quality = quality;
    }

    /** Returns the SM-2 quality score (0–5) for this rating. */
    public int toQuality() {
        return quality;
    }

    /**
     * Case-insensitive JSON deserializer.
     * Accepts "again", "AGAIN", "Again", etc.
     */
    @JsonCreator
    public static ReviewRating fromValue(String value) {
        for (ReviewRating r : values()) {
            if (r.name().equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Rating không hợp lệ: " + value +
                ". Giá trị hợp lệ: again, hard, good, easy");
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
