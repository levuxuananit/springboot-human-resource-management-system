package com.r2s.user.security;

import com.r2s.user.service.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");

        // [?] 1. No header or the Bearer format is not correct, continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return; // [?] Finish processing this Filter here
        }

        // [?] 2. Extract Token (remove the word "Bearer")
        String token = authHeader.substring(7);

        try {

            // [?] 3. get username from token
            String username = jwtUtil.extractUsername(token);

            // [?] 4. If the username is present and not authenticated in this session (SecurityContext)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // [?] Find user in Database
                UserDetails userDetails = userDetailService.loadUserByUsername(username);

                // [?] 5. Validate token and set authentication
                if (jwtUtil.isTokenValid(token)) {

                    // [?] Create authentication object for Spring Security
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    // [?] Save request info (IP, SessionID) into authToken
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                    // [?] From this moment, annotations like @PreAuthorize will take effect
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // [?] If there is an error (fake token, expired...), delete the context to ensure safety
            SecurityContextHolder.clearContext();
        }

        // [?] 6. Always call chain.doFilter to request to continue the journey
        chain.doFilter(req, res);
    }
}

