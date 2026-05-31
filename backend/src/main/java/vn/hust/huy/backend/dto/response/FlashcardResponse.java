package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.Flashcard;
import vn.hust.huy.backend.model.entity.SrsDetail;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for a single flashcard.
 *
 * <p>{@code srsInfo} is populated only on single-card endpoints
 * (GET /{id}, POST create, PATCH review). It is {@code null} on list endpoints
 * to avoid N+1 queries when loading many cards.
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

    /** SRS state — present only on single-card responses. */
    private SrsInfoDto srsInfo;

    // ── Factory methods ────────────────────────────────────────────────────────

    /** Lightweight mapping used by list endpoints (no SRS data). */
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

    /** Full mapping including SRS state — used by single-card endpoints. */
    public static FlashcardResponse fromEntityWithSrs(Flashcard card, SrsDetail srs) {
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
                .srsInfo(SrsInfoDto.fromEntity(srs, card.getStatus()))
                .build();
    }
}
