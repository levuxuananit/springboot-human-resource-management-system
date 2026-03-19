package com.r2s.auth.service;

import com.r2s.auth.dto.AuthResponseDTO;
import com.r2s.auth.dto.LoginRequestDTO;
import com.r2s.auth.dto.RegisterRequestDTO;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setRole(Role.USER);

        userRepo.save(user);
    }

    // [!] -------------------- Login -----------------------
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        return new AuthResponseDTO(token);
    }
}
