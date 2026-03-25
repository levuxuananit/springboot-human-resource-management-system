package com.r2s.user.dto;

import com.r2s.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private final String username;
    private final String password;

    public static UserResponseDTO fromEntity(User user) {
        return UserResponseDTO
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
