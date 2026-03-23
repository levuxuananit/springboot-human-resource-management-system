package com.r2s.auth.service;

import com.r2s.auth.dto.RegisterRequestDTO;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
// [?]: @RequiredArgsConstructor uses "final" fields to ensure they must be initialized when the object is created
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [!] -------------------- Register --------------------
    public void register(RegisterRequestDTO requestDTO) {
        if (userRepo.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("User name already exists");
        }

        User user = new User();

        user.setUsername(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword())); //[?]: hash password by BCrypt before save to DB
        user.setCreatedAt(LocalDateTime.now());
        userRepo.save(user);
    }
}
