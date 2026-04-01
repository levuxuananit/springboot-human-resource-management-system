package com.r2s.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserProvider userProvider;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userProvider.loadUserByUsername(username);
    }
}
