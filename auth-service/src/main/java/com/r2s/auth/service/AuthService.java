package com.r2s.auth.service;

import com.r2s.auth.dto.AuthResponse;
import com.r2s.auth.dto.LoginRequest;
import com.r2s.auth.dto.RegisterRequest;
import com.r2s.core.config.SecurityConstants;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.core.exception.DuplicateResourceException;
import com.r2s.core.exception.InvalidCredentialsException;
import com.r2s.core.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

// [?]: @RequiredArgsConstructor uses "final" fields to ensure they must be initialized when the object is created
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [!] -------------------- Register --------------------
    public void register(RegisterRequest req) {

        if (repo.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .role(Role.USER)
                .build();

        repo.save(user);
    }

    // [!] -------------------- Login -----------------------
    public AuthResponse login(LoginRequest req) {
        User user = repo
                .findByUsername(req.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .username(user.getUsername())
                .expiresIn(SecurityConstants.EXPIRATION_TIME)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
