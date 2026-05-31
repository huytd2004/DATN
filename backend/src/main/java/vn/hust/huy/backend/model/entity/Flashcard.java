package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.hust.huy.backend.model.converter.FlashcardStatusConverter;
import vn.hust.huy.backend.model.enums.FlashcardStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * A single flashcard inside a {@link FlashcardDeck}.
 * Content is free-form (front/back) rather than linked to dictionary_entries.
 * Maps to the {@code flashcards} table.
 */
@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Relationships ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private FlashcardDeck deck;

    @OneToOne(mappedBy = "flashcard", fetch = FetchType.LAZY)
    private SrsDetail srsDetail;

    // ── Card content ───────────────────────────────────────────────────────────

    @Column(name = "front_text", nullable = false, columnDefinition = "TEXT")
    private String frontText;

    @Column(name = "front_reading", columnDefinition = "TEXT")
    private String frontReading;

    @Column(name = "back_text", nullable = false, columnDefinition = "TEXT")
    private String backText;

    @Column(name = "back_notes", columnDefinition = "TEXT")
    private String backNotes;

    // ── Status ─────────────────────────────────────────────────────────────────
    // @Convert maps NEW_CARD ↔ "new", LEARNING ↔ "learning", REVIEW ↔ "review".
    // DB column is varchar(20) (not a PostgreSQL named enum) so Hibernate can
    // bind the string value directly without any cast.
    @Convert(converter = FlashcardStatusConverter.class)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FlashcardStatus status = FlashcardStatus.NEW_CARD;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
