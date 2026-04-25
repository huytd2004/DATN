package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

/**
 * Request body for creating a new flashcard inside a deck.
 */
@Getter
public class FlashcardRequest {

    @NotNull(message = "Deck ID không được để trống")
    private UUID deckId;

    @NotBlank(message = "Mặt trước không được để trống")
    private String frontText;

    private String frontReading;

    @NotBlank(message = "Mặt sau không được để trống")
    private String backText;

    private String backNotes;
}
