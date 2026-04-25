package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.FlashcardDeck;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for a flashcard deck.
 */
@Getter
@Builder
public class FlashcardDeckResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private boolean isPublic;
    private Instant createdAt;

    public static FlashcardDeckResponse fromEntity(FlashcardDeck deck) {
        return FlashcardDeckResponse.builder()
                .id(deck.getId())
                .ownerId(deck.getUser().getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .isPublic(deck.isPublic())
                .createdAt(deck.getCreatedAt())
                .build();
    }
}
