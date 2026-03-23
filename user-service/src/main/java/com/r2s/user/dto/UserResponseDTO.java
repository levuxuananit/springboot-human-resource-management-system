package com.r2s.user.dto;

import com.r2s.user.entity.Role;
import com.r2s.user.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private String username;
    private Role role;

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO res = new UserResponseDTO();
        res.setUsername(user.getUsername());
        res.setRole(user.getRole());
        return res;
    }
}
