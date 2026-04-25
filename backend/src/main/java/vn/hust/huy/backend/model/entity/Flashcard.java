package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "flashcard_status")
    @Builder.Default
    private FlashcardStatus status = FlashcardStatus.learning;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
