package vn.hust.huy.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Learning status of a flashcard.
 *
 * <p>Maps to PostgreSQL enum type {@code flashcard_status}.
 * Java constant names use uppercase (NEW_CARD, LEARNING, REVIEW)
 * because {@code new} is a reserved keyword in Java.
 * The JSON / DB wire value is lowercase ("new", "learning", "review").
 *
 * <table>
 *   <tr><th>Status</th><th>Meaning</th></tr>
 *   <tr><td>new</td>     <td>Card created, never reviewed</td></tr>
 *   <tr><td>learning</td><td>Actively being learnt (short intervals, or after a failed review)</td></tr>
 *   <tr><td>review</td>  <td>Graduated — scheduled for spaced-repetition review (repetitions ≥ 2)</td></tr>
 * </table>
 */
public enum FlashcardStatus {

    NEW_CARD("new"),
    LEARNING("learning"),
    REVIEW("review");

    private final String value;

    FlashcardStatus(String value) {
        this.value = value;
    }

    /** Wire value used for both JSON serialization and DB storage. */
    @JsonValue
    public String getValue() {
        return value;
    }

    /** Case-insensitive JSON deserialization from "new" / "learning" / "review". */
    @JsonCreator
    public static FlashcardStatus fromValue(String value) {
        if (value == null) return null;
        for (FlashcardStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid flashcard status: \"" + value +
                "\". Valid values: new, learning, review");
    }
}
