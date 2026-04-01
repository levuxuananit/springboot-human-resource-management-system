package com.r2s.user.controller;

import com.r2s.user.dto.UpdateUserRequest;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // [!] -------------------- Read all users --------------------
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // [!] -------------------- Read own profile ------------------
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByUsername(authentication.getName()));
    }

    // [!] -------------------- Update own profile ----------------
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UpdateUserRequest req, Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateUser(username, req));
    }

    // [!] -------------------- Delete ----------------------------
    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
