package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.request.LoginRequest;
import vn.hust.huy.backend.dto.request.RefreshRequest;
import vn.hust.huy.backend.dto.request.RegisterRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.AuthResponse;
import vn.hust.huy.backend.dto.response.UserResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.RefreshToken;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.RefreshTokenRepository;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.security.CustomUserDetailsService;
import vn.hust.huy.backend.security.JwtTokenProvider;
import vn.hust.huy.backend.service.AuthService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ── Register ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS); // reuse or add USERNAME_TAKEN error
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : vn.hust.huy.backend.model.enums.Role.USER)
                .targetLevel(request.getTargetLevel() != null ? request.getTargetLevel() : vn.hust.huy.backend.model.enums.JlptLevel.N5)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {}", user.getEmail());

        return ApiResponse.success(UserResponse.fromEntity(user), "Đăng ký thành công", 201);
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, user.getId(), user.getRole());
        String refreshTokenValue = createAndSaveRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return ApiResponse.success(
                AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshTokenValue)
                        .build(),
                "Đăng nhập thành công"
        );
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<AuthResponse> refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails, user.getId(), user.getRole());

        log.debug("Token refreshed for user: {}", user.getEmail());

        return ApiResponse.success(
                AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken.getToken())
                        .build(),
                "Token đã được làm mới"
        );
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> logout(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        refreshTokenRepository.delete(refreshToken);
        log.info("User logged out: {}", refreshToken.getUser().getEmail());

        return ApiResponse.success(null, "Đăng xuất thành công");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String createAndSaveRefreshToken(User user) {
        // Chỉ xóa các token đã hết hạn, giữ lại token còn hợp lệ (multi-tab support)
        refreshTokenRepository.deleteExpiredByUser(user, Instant.now());

        String tokenValue = jwtTokenProvider.generateRefreshTokenValue();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(token);
        return tokenValue;
    }
}
