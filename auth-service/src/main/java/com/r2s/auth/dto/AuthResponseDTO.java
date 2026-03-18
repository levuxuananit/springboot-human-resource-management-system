package com.r2s.auth.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
}
