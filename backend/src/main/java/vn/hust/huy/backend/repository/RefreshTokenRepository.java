package vn.hust.huy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hust.huy.backend.model.entity.RefreshToken;
import vn.hust.huy.backend.model.entity.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    /**
     * Xóa các refresh token đã hết hạn của user (giữ lại token còn hiệu lực).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user AND r.expiryDate < :now")
    void deleteExpiredByUser(@Param("user") User user, @Param("now") Instant now);
}
