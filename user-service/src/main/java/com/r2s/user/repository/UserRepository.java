package com.r2s.user.repository;

import com.r2s.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // [?]: when login, returning optional to avoid NullPointerException
    Optional<User> findByUsername(String username);

    // [?]: when register, checking username exists instead of "select *" (optimize performance)
    boolean existsByUsername(String username);
}
