package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for a single flashcard.
 */
@Getter
@Builder
public class FlashcardResponse {

    private UUID id;
    private UUID deckId;
    private String deckName;
    private String frontText;
    private String frontReading;
    private String backText;
    private String backNotes;
    private FlashcardStatus status;
    private Instant createdAt;

    public static FlashcardResponse fromEntity(Flashcard card) {
        return FlashcardResponse.builder()
                .id(card.getId())
                .deckId(card.getDeck().getId())
                .deckName(card.getDeck().getName())
                .frontText(card.getFrontText())
                .frontReading(card.getFrontReading())
                .backText(card.getBackText())
                .backNotes(card.getBackNotes())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .build();
    }
}
