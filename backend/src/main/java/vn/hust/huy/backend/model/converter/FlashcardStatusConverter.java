package vn.hust.huy.backend.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

/**
 * JPA AttributeConverter that maps {@link FlashcardStatus} ↔ the PostgreSQL
 * {@code flashcard_status} enum stored as a string column.
 *
 * <p>This converter is necessary because the Java constant {@code NEW_CARD}
 * must map to the DB/API value {@code "new"} — a Java reserved keyword that
 * cannot be used as an enum constant name.
 *
 * <p>{@code autoApply = true} means every {@code FlashcardStatus} field in
 * any JPA entity is automatically handled without needing per-field annotations.
 */
@Converter(autoApply = true)
public class FlashcardStatusConverter implements AttributeConverter<FlashcardStatus, String> {

    @Override
    public String convertToDatabaseColumn(FlashcardStatus status) {
        if (status == null) return null;
        return status.getValue();           // NEW_CARD → "new", etc.
    }

    @Override
    public FlashcardStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return FlashcardStatus.fromValue(dbValue);  // "new" → NEW_CARD, etc.
    }
}
