package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.SrsDetail;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

import java.time.Instant;

/**
 * Nested SRS (spaced-repetition) state embedded inside {@link FlashcardResponse}.
 * Returned only for single-card endpoints (GET /{id}, POST, PATCH review).
 */
@Getter
@Builder
public class SrsInfoDto {

    private double easeFactor;
    private int intervalDays;
    private int repetitions;
    private Instant nextReview;
    private FlashcardStatus status;

    public static SrsInfoDto fromEntity(SrsDetail srs, FlashcardStatus status) {
        return SrsInfoDto.builder()
                .easeFactor(srs.getEaseFactor())
                .intervalDays(srs.getIntervalDays())
                .repetitions(srs.getRepetitions())
                .nextReview(srs.getNextReview())
                .status(status)
                .build();
    }
}
