package com.r2s.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "Username can not be blank")
    private final String username;
    @NotBlank(message = "Password can bot be blank")
    private final String password;
}
