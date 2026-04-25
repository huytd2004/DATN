package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Request body for creating or updating a flashcard deck.
 */
@Getter
public class FlashcardDeckRequest {

    @NotBlank(message = "Tên bộ thẻ không được để trống")
    private String name;

    private String description;

    private boolean isPublic = false;
}
