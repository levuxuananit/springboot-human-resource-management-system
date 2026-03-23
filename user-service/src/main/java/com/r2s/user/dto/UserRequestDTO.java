package com.r2s.user.dto;

import com.r2s.user.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {
    private String username;
    private String password;
    private Role role;
}
