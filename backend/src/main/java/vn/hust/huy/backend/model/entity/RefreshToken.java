package vn.hust.huy.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores active refresh tokens for JWT rotation.
 * Not part of the main domain schema but required for auth.
 * Maps to the {@code app_refresh_tokens} table (kept separate from domain tables).
 */
@Entity
@Table(name = "app_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}
