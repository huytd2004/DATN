package vn.hust.huy.backend.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Pure-function implementation of the SuperMemo 2 (SM-2) spaced-repetition algorithm.
 *
 * <p>Algorithm reference: <a href="https://www.supermemo.com/en/archives1990-2015/english/ol/sm2">
 *     SuperMemo SM-2 (Piotr Woźniak, 1990)</a>
 *
 * <p>This class is stateless — all state is passed in and returned via {@link ReviewResult}.
 */
public final class SM2Algorithm {

    /** Minimum allowed E-Factor (easiness factor). */
    private static final double MIN_EF = 1.3;

    /** Default starting E-Factor for a new card. */
    public static final double DEFAULT_EF = 2.5;

    private SM2Algorithm() {
        // utility class — no instantiation
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Calculates the new SM-2 state for a flashcard after a single review.
     *
     * <p>Quality scale (0–5):
     * <ul>
     *   <li>5 — Perfect response, no hesitation</li>
     *   <li>4 — Correct, after slight hesitation</li>
     *   <li>3 — Correct, with significant difficulty</li>
     *   <li>2 — Incorrect, but answer felt familiar</li>
     *   <li>1 — Incorrect, answer barely recognized</li>
     *   <li>0 — Complete blackout</li>
     * </ul>
     *
     * @param ef       current E-Factor (easiness factor), must be ≥ 1.3
     * @param n        current repetition count (number of consecutive successful reviews)
     * @param interval current interval in days
     * @param quality  review quality score, must be in [0, 5]
     * @return a {@link ReviewResult} with updated SM-2 parameters
     */
    public static ReviewResult calculate(double ef, int n, int interval, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException(
                    "Quality phải trong khoảng [0, 5], nhận được: " + quality);
        }

        // Step 1: Update E-Factor
        double newEf = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        newEf = Math.max(MIN_EF, newEf);

        int newN;
        int newInterval;

        if (quality >= 3) {
            // Successful recall — advance the schedule
            newInterval = switch (n) {
                case 0  -> 1;
                case 1  -> 6;
                default -> (int) Math.round(interval * newEf);
            };
            newN = n + 1;
        } else {
            // Failed recall — reset to the beginning
            newN = 0;
            newInterval = 1;
        }

        Instant nextReview = Instant.now().plus(newInterval, ChronoUnit.DAYS);

        return new ReviewResult(newEf, newN, newInterval, nextReview);
    }

    // ── Result record ──────────────────────────────────────────────────────────

    /**
     * Immutable result of a single SM-2 review calculation.
     *
     * @param easeFactor   new E-Factor after this review
     * @param repetitions  new repetition count
     * @param intervalDays new interval in days until the next review
     * @param nextReview   absolute timestamp of the next review
     */
    public record ReviewResult(
            double easeFactor,
            int repetitions,
            int intervalDays,
            Instant nextReview
    ) {}
}
