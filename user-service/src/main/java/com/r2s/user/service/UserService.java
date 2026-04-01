package com.r2s.user.service;

import com.r2s.core.exception.DuplicateResourceException;
import com.r2s.core.exception.ResourceNotFoundException;
import com.r2s.user.dto.UpdateUserRequest;
import com.r2s.user.dto.UserResponse;
import com.r2s.core.entity.User;
import com.r2s.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    // [!] -------------------- Read all users --------------------
    public List<UserResponse> getAllUsers() {
        return repo
                .findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList()
                );
    }

    // [!] -------------------- Get user by name -------------------
    public UserResponse getUserByUsername(String username) {
        return repo
                .findByUsername(username)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // [!] -------------------- Update user by name ----------------
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest req) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // [?] Update new email
        if(!req.getEmail().equals(user.getEmail())){
            if(repo.existsByEmail(req.getEmail())){
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(req.getEmail());
        }
        // [?] Update new fullName
        if(!req.getFullName().equals(user.getFullName())){
            user.setFullName(req.getFullName());
        }

        return UserResponse.fromEntity(repo.save(user));
    }

    // [!] -------------------- Delete user by name ----------------
    @Transactional
    public void deleteUser(String username) {
        User user = repo
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        repo.delete(user);
    }
}
