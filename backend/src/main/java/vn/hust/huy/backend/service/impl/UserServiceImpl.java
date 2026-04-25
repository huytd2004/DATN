package vn.hust.huy.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hust.huy.backend.dto.response.ApiResponse;
import vn.hust.huy.backend.dto.response.UserResponse;
import vn.hust.huy.backend.exception.AppException;
import vn.hust.huy.backend.exception.ErrorCode;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.repository.UserRepository;
import vn.hust.huy.backend.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserResponse> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ApiResponse.success(UserResponse.fromEntity(user), "Lấy thông tin người dùng thành công");
    }
}
