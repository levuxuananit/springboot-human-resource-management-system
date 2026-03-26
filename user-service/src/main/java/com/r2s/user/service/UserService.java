package com.r2s.user.service;

import com.r2s.core.exception.DuplicateResourceException;
import com.r2s.core.exception.ResourceNotFoundException;
import com.r2s.user.dto.UpdateRequestDTO;
import com.r2s.user.dto.UserResponseDTO;
import com.r2s.core.entity.User;
import com.r2s.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public List<UserResponseDTO> getAllUsers() {
        return repo
                .findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList()
                );
    }

    // [!] -------------------- Get user by name -------------------
    public UserResponseDTO getUserByUsername(String username) {
        return repo
                .findByUsername(username)
                .map(UserResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // [!] -------------------- Update user by name ----------------
    @Transactional
    public UserResponseDTO updateUser(String currentUsername, UpdateRequestDTO dto) {
        User user = repo.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            String newUsername = dto.getUsername();

            if (!newUsername.equals(currentUsername)) {
                if (repo.existsByUsername(newUsername)) {
                    throw new DuplicateResourceException("User has already taken!");
                }
                user.setUsername(newUsername);
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return UserResponseDTO.fromEntity(repo.save(user));
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
