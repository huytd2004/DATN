package vn.hust.huy.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.hust.huy.backend.model.entity.User;
import vn.hust.huy.backend.model.enums.JlptLevel;
import vn.hust.huy.backend.model.enums.Role;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private Role role;
    private JlptLevel targetLevel;
    private Instant createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .targetLevel(user.getTargetLevel())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
