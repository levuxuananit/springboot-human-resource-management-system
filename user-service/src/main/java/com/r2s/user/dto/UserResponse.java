package com.r2s.user.dto;

import com.r2s.core.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponse {
    private final String username;
    private final String role;
    private final String email;
    private final String fullName;

    public static UserResponse fromEntity(User user) {
        return UserResponse
                .builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
