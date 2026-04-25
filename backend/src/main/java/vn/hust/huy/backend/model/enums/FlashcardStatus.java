package vn.hust.huy.backend.model.enums;

/**
 * Learning status of a flashcard.
 * Maps to PostgreSQL enum type {@code flashcard_status}.
 */
public enum FlashcardStatus {
    learning,
    reviewing,
    mastered
}
