package vn.hust.huy.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Request body for {@code PATCH /api/v1/flashcards/{id}/review}.
 *
 * <p>Example payload:
 * <pre>{@code { "rating": "good" }}</pre>
 */
@Getter
public class ReviewRequest {

    @NotNull(message = "Rating không được để trống (again | hard | good | easy)")
    private ReviewRating rating;
}
