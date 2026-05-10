package com.r2s.auth.dto;

import com.r2s.core.config.SecurityConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = SecurityConstants.USERNAME_REQUIRED)
    @Size(min = 3, max = 20, message = SecurityConstants.USERNAME_SIZE)
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = SecurityConstants.USERNAME_INVALID)
    private String username;

    @NotBlank(message = SecurityConstants.PASSWORD_REQUIRED)
    @Size(min = 6, message = SecurityConstants.PASSWORD_SIZE)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = SecurityConstants.PASSWORD_COMPLEXITY)
    private String password;

    @NotBlank(message = SecurityConstants.EMAIL_REQUIRED)
    @Email(message = SecurityConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = SecurityConstants.FULLNAME_REQUIRED)
    private String fullName;
}
