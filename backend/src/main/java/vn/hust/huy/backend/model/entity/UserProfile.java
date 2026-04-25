package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Extended profile data for a user. One-to-one with {@link User}.
 * Maps to the {@code user_profiles} table.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    /** Shares PK with {@code users.id}. */
    @Id
    private java.util.UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "daily_goal_minutes", nullable = false)
    @Builder.Default
    private int dailyGoalMinutes = 30;

    @Column(name = "streak_count", nullable = false)
    @Builder.Default
    private int streakCount = 0;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private int totalPoints = 0;

    @Column(name = "last_active")
    private Instant lastActive;
}
