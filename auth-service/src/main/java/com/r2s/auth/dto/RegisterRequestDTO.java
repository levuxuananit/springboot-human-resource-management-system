package com.r2s.auth.dto;

import com.r2s.auth.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {
    private String username;
    private String password;
    private Role role;
}
