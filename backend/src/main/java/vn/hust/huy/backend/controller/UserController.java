package vn.hust.huy.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.UserResponse;
import vn.hust.huy.backend.service.UserService;

/**
 * User profile endpoints — all require a valid JWT.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /users/me
     * Returns the profile of the currently authenticated user.
     * Accessible by both USER and ADMIN roles.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }
}
