package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Per-deck card count breakdown, embedded inside {@link FlashcardDeckResponse}.
 *
 * <p>Definitions:
 * <ul>
 *   <li>{@code newCount}      — cards never reviewed (status = "new")</li>
 *   <li>{@code learningCount} — cards in active learning (status = "learning")</li>
 *   <li>{@code reviewCount}   — cards in spaced-repetition review queue (status = "review")</li>
 *   <li>{@code masteredCount} — cards with status = "review" (same as reviewCount by definition)</li>
 *   <li>{@code total}         — newCount + learningCount + reviewCount</li>
 *   <li>{@code dueToday}      — learningCount + reviewCount (cards needing attention)</li>
 *   <li>{@code progressPercent} — (learningCount + reviewCount) / total × 100 (0 when total = 0)</li>
 *   <li>{@code masteredPercent} — masteredCount / total × 100 (0 when total = 0)</li>
 * </ul>
 */
@Getter
@Builder
public class CardStatsDto {

    private int total;
    private int newCount;
    private int learningCount;
    private int reviewCount;
    private int masteredCount;

    /** Cards currently needing study = learningCount + reviewCount. */
    private int dueToday;

    /**
     * Learning progress percentage.
     * Formula: (learningCount + reviewCount) / total × 100.
     */
    private int progressPercent;

    /**
     * Mastered progress percentage.
     * Formula: masteredCount / total × 100.
     */
    private int masteredPercent;

    // ── Factory ────────────────────────────────────────────────────────────────

    public static CardStatsDto of(int newCount, int learningCount, int reviewCount, int masteredCount) {
        int total = newCount + learningCount + reviewCount;
        int dueToday = learningCount + reviewCount;
        int progressPercent = (total > 0) ? Math.round(dueToday * 100f / total) : 0;
        int masteredPercent = (total > 0) ? Math.round(masteredCount * 100f / total) : 0;

        return CardStatsDto.builder()
                .total(total)
                .newCount(newCount)
                .learningCount(learningCount)
                .reviewCount(reviewCount)
                .masteredCount(masteredCount)
                .dueToday(dueToday)
                .progressPercent(progressPercent)
                .masteredPercent(masteredPercent)
                .build();
    }

    /** Fallback when masteredCount not provided — defaults to reviewCount. */
    public static CardStatsDto of(int newCount, int learningCount, int reviewCount) {
        return of(newCount, learningCount, reviewCount, reviewCount);
    }
}
