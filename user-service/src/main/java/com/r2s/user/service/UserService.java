package com.r2s.user.service;

import com.r2s.user.dto.UserRequestDTO;
import com.r2s.user.dto.UserResponseDTO;
import com.r2s.user.entity.User;
import com.r2s.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    // [?]: -------------------- READ --------------------
    public List<UserResponseDTO> getALlUser(){
        return userRepo.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getByUsername(String name){
        return userRepo.findByUsername(name)
                .map(UserResponseDTO::fromEntity)
                .orElseThrow(()-> new UsernameNotFoundException("User name not found"));
    }

    // [?]: -------------------- UPDATE ------------------
    public UserResponseDTO updateUser(String username, UserRequestDTO userRes){
        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User name not found"));

        user.setUsername(userRes.getUsername());
        user.setRole(userRes.getRole());

        return UserResponseDTO.fromEntity(userRepo.save(user));
    }

    // [?]: -------------------- DELETE ------------------
    public void deleteUser(String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }
        userRepo.deleteByUsername(username);
    }

}
