package vn.hust.huy.backend.service;

import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.UserResponse;

public interface UserService {

    ApiResponse<UserResponse> getCurrentUser(String email);
}
