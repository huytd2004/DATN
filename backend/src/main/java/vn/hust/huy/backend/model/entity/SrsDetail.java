package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * SM-2 spaced-repetition state for a single flashcard.
 * One-to-one with {@link Flashcard}; stored in the {@code srs_details} table.
 */
@Entity
@Table(name = "srs_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrsDetail {

    /** Shared PK with flashcards.id */
    @Id
    private UUID flashcardId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;

    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    @Column(nullable = false)
    @Builder.Default
    private int repetitions = 0;

    @Column(name = "next_review", nullable = false)
    @Builder.Default
    private Instant nextReview = Instant.now();
}
