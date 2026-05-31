package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Final learning summary stored after a tutor session ends.
 * Maps to the {@code tutor_session_results} table.
 */
@Entity
@Table(name = "tutor_session_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorSessionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ConversationSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "user_turns")
    private Integer userTurns;

    @Column(name = "assistant_turns")
    private Integer assistantTurns;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "mistake_count", nullable = false)
    @Builder.Default
    private Integer mistakeCount = 0;

    @Column(name = "correction_count", nullable = false)
    @Builder.Default
    private Integer correctionCount = 0;

    @Column(name = "fluency_score")
    private Integer fluencyScore;

    @Column(name = "accuracy_score")
    private Integer accuracyScore;

    @Column(name = "pronunciation_score")
    private Integer pronunciationScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mistakes", columnDefinition = "jsonb")
    private String mistakes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_vocabulary", columnDefinition = "jsonb")
    private String newVocabulary;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "finished_at", nullable = false)
    @Builder.Default
    private Instant finishedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}