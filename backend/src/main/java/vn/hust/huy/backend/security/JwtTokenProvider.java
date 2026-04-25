package vn.hust.huy.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import vn.hust.huy.backend.model.enums.Role;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Handles JWT creation, parsing, and validation.
 *
 * <p>Token payload:
 * <ul>
 *   <li>{@code sub}    – user email</li>
 *   <li>{@code userId} – user UUID (for Activity Manager, SRS Engine, AI Bridge)</li>
 *   <li>{@code role}   – user role (USER | ADMIN)</li>
 *   <li>{@code iat}    – issued-at timestamp</li>
 *   <li>{@code exp}    – expiry timestamp</li>
 * </ul>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // ── Key ────────────────────────────────────────────────────────────────────

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // ── Token Generation ───────────────────────────────────────────────────────

    /**
     * Generates a signed access token embedding userId and role.
     */
    public String generateAccessToken(UserDetails userDetails, UUID userId, Role role) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userId.toString())
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Generates an opaque refresh token (stored in DB).
     */
    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    // ── Claims Extraction ──────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the token is structurally valid, correctly signed, and not expired.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT security error: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT illegal argument: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername()) && validateToken(token);
    }
}
