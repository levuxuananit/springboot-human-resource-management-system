package com.r2s.user.dto;

import com.r2s.core.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private final String username;

    public static UserResponseDTO fromEntity(User user) {
        return UserResponseDTO
                .builder()
                .username(user.getUsername())
                .build();
    }
}
