package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A single chat message (user or assistant) in a {@link ConversationSession}.
 * Maps to the {@code learning_logs} table.
 */
@Entity
@Table(name = "learning_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    /** "user" or "assistant". */
    @Column(length = 50)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
