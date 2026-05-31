package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.FlashcardDeck;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Read model for a flashcard deck.
 *
 * <p>{@code cardStats} is always populated when fetching decks via
 * {@code GET /api/v1/decks} — it contains per-deck card counts and
 * progress metrics computed in a single aggregate query.
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

    /** Per-deck card statistics — always present on list endpoints. */
    private CardStatsDto cardStats;

    // ── Factory methods ────────────────────────────────────────────────────────

    /**
     * Maps a deck entity and its pre-aggregated status counts to a response.
     *
     * @param deck         the deck entity
     * @param statusCounts map of status string → count
     *                     (keys: "new", "learning", "review")
     * @param masteredCount count of mastered cards (status = "review")
     */
    public static FlashcardDeckResponse fromEntityWithStats(
            FlashcardDeck deck, Map<String, Long> statusCounts, long masteredCount) {

        int newCount      = statusCounts.getOrDefault("new",      0L).intValue();
        int learningCount = statusCounts.getOrDefault("learning", 0L).intValue();
        int reviewCount   = statusCounts.getOrDefault("review",   0L).intValue();

        return FlashcardDeckResponse.builder()
                .id(deck.getId())
                .ownerId(deck.getUser().getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .isPublic(deck.isPublic())
                .createdAt(deck.getCreatedAt())
                .cardStats(CardStatsDto.of(newCount, learningCount, reviewCount, (int) masteredCount))
                .build();
    }

        /**
         * Factory variant that allows specifying an explicit dueToday override (computed from SRS nextReview).
         */
        public static FlashcardDeckResponse fromEntityWithStatsAndDue(
            FlashcardDeck deck, Map<String, Long> statusCounts, long masteredCount, int dueTodayOverride) {

        int newCount      = statusCounts.getOrDefault("new",      0L).intValue();
        int learningCount = statusCounts.getOrDefault("learning", 0L).intValue();
        int reviewCount   = statusCounts.getOrDefault("review",   0L).intValue();

        int total = newCount + learningCount + reviewCount;
        int mastered = (int) masteredCount;
        int progressPercent = (total > 0) ? Math.round(dueTodayOverride * 100f / total) : 0;
        int masteredPercent = (total > 0) ? Math.round(mastered * 100f / total) : 0;

        CardStatsDto stats = CardStatsDto.builder()
            .total(total)
            .newCount(newCount)
            .learningCount(learningCount)
            .reviewCount(reviewCount)
            .masteredCount(mastered)
            .dueToday(dueTodayOverride)
            .progressPercent(progressPercent)
            .masteredPercent(masteredPercent)
            .build();

        return FlashcardDeckResponse.builder()
            .id(deck.getId())
            .ownerId(deck.getUser().getId())
            .name(deck.getName())
            .description(deck.getDescription())
            .isPublic(deck.isPublic())
            .createdAt(deck.getCreatedAt())
            .cardStats(stats)
            .build();
        }

    /**
     * Overload for backward compatibility when no mastered count is provided.
     * Falls back to mastered = reviewCount (since status='review' means mastered in SRS).
     */
    public static FlashcardDeckResponse fromEntityWithStats(
            FlashcardDeck deck, Map<String, Long> statusCounts) {
        int reviewCount = statusCounts.getOrDefault("review", 0L).intValue();
        return fromEntityWithStats(deck, statusCounts, reviewCount);
    }

    /** Lightweight factory (no stats) — for newly created decks. */
    public static FlashcardDeckResponse fromEntity(FlashcardDeck deck) {
        return fromEntityWithStats(deck, Map.of(), 0);
    }
}
