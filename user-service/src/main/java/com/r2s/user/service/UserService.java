package com.r2s.user.service;

import com.r2s.core.exception.ConflictException;
import com.r2s.core.exception.NotFoundException;
import com.r2s.user.dto.UpdateUserRequest;
import com.r2s.user.dto.UserResponse;
import com.r2s.core.entity.User;
import com.r2s.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repo;

    // [!] -------------------- Read all users --------------------
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        log.info("Fetching users with pagination: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        return repo
                .findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    // [!] -------------------- Get user by name -------------------
    public UserResponse getUserByUsername(String username) {
        return repo
                .findByUsername(username)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    // [!] -------------------- Update user by name ----------------
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest req) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // [?] Update new email
        if(!req.getEmail().equals(user.getEmail())){
            if(repo.existsByEmail(req.getEmail())){
                throw new ConflictException("Email already exists");
            }
            user.setEmail(req.getEmail());
        }
        // [?] Update new fullName
        if(!req.getFullName().equals(user.getFullName())){
            user.setFullName(req.getFullName());
        }

        log.info("Updating profile for user: {}", username);
        return UserResponse.fromEntity(repo.save(user));
    }

    // [!] -------------------- Delete user by name ----------------
    @Transactional
    public void deleteUser(String username) {
        User user = repo
                .findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        repo.delete(user);
    }
}
