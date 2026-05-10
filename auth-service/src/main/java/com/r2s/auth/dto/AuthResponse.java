package com.r2s.auth.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private final String accessToken;

    @Builder.Default
    private final String tokenType = "Bearer";
    private final int expiresIn;
    private final String username;
    private final UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String email;
        private final String role;
    }

}
