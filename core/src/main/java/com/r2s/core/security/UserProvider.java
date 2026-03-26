package com.r2s.core.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserProvider {
    UserDetails loadUserByUsername(String username);
}
