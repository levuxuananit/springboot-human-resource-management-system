package com.r2s.user.controller;

import com.r2s.user.dto.UserRequestDTO;
import com.r2s.user.dto.UserResponseDTO;
import com.r2s.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getALlUser());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getByUsername(username));
    }

    @PutMapping("profile")
    public ResponseEntity<UserResponseDTO> updateMyProfile(@RequestBody UserRequestDTO res, Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateUser(username, res));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

}
