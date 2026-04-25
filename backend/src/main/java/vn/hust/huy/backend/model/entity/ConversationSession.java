package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * An AI conversation session for a user.
 * Maps to the {@code conversation_sessions} table.
 */
@Entity
@Table(name = "conversation_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scenario_name", length = 255)
    private String scenarioName;

    /** JSON array of target word IDs / strings for this session. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_words", columnDefinition = "jsonb")
    private String targetWords;

    @Column(name = "started_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;
}
