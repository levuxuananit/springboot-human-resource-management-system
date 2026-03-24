package com.r2s.auth.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private final String accessToken;

    @Builder.Default
    private final String tokenType = "Bearer";
    private final Long expiresIn;
    private final String username;
    private final String role;

}
