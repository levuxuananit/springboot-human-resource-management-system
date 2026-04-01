package com.r2s.user.dto;

import com.r2s.core.config.SecurityConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    @NotBlank(message = SecurityConstants.EMAIL_REQUIRED)
    @Email(message = SecurityConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = SecurityConstants.FULLNAME_REQUIRED)
    private String fullName;
}
