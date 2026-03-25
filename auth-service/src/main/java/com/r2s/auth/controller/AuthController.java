package com.r2s.auth.controller;

import com.r2s.auth.dto.AuthResponseDTO;
import com.r2s.auth.dto.LoginRequestDTO;
import com.r2s.auth.dto.RegisterRequestDTO;
import com.r2s.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // [!] -------------------- Register --------------------
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO requestDTO){
        authService.register(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    // [!] -------------------- Login -----------------------
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(request));
    }

}
