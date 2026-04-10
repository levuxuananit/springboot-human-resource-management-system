package com.r2s.auth.service;

import com.r2s.auth.dto.AuthResponse;
import com.r2s.auth.dto.LoginRequest;
import com.r2s.auth.dto.RegisterRequest;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.core.exception.ConflictException;
import com.r2s.core.exception.UnauthenticatedException;
import com.r2s.core.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

// [?]: @RequiredArgsConstructor uses "final" fields to ensure they must be initialized when the object is created
public class AuthService {
    @Value("${jwt.expiration}")
    private int expiration;
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [!] -------------------- Register --------------------
    public void register(@Valid RegisterRequest req) {

        if (repo.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        if (repo.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists");
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
        User user = repo.findByUsername(req.getUsername())
                .orElseThrow(() -> new UnauthenticatedException("Invalid username or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .username(user.getUsername())
                .expiresIn(expiration)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
