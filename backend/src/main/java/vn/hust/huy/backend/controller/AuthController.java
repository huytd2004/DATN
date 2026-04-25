package vn.hust.huy.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.request.LoginRequest;
import vn.hust.huy.backend.dto.request.RefreshRequest;
import vn.hust.huy.backend.dto.request.RegisterRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.AuthResponse;
import vn.hust.huy.backend.dto.response.UserResponse;
import vn.hust.huy.backend.service.AuthService;

/**
 * Handles all authentication endpoints.
 * All routes under {@code /auth/**} are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/register
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        ApiResponse<UserResponse> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login
     * Authenticate user and return access + refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/refresh
     * Exchange a valid refresh token for a new access token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * POST /auth/logout
     * Invalidate the provided refresh token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(authService.logout(request));
    }
}
