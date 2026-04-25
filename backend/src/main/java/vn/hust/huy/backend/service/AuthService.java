package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.request.LoginRequest;
import vn.hust.huy.backend.dto.request.RefreshRequest;
import vn.hust.huy.backend.dto.request.RegisterRequest;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.AuthResponse;
import vn.hust.huy.backend.dto.response.UserResponse;

public interface AuthService {

    ApiResponse<UserResponse> register(RegisterRequest request);

    ApiResponse<AuthResponse> login(LoginRequest request);

    ApiResponse<AuthResponse> refresh(RefreshRequest request);

    ApiResponse<Void> logout(RefreshRequest request);
}
