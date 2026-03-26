package com.r2s.auth.service;

import com.r2s.auth.dto.AuthResponseDTO;
import com.r2s.auth.dto.LoginRequestDTO;
import com.r2s.auth.dto.RegisterRequestDTO;
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
    public void register(RegisterRequestDTO dto) {

        if (repo.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build();

        repo.save(user);
    }

    // [!] -------------------- Login -----------------------
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = repo
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .username(user.getUsername())
                .expiresIn(SecurityConstants.EXPIRATION_TIME)
                .role(user.getRole().name())
                .build();
    }
}
