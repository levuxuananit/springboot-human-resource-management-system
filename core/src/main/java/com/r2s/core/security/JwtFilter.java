package com.r2s.core.security;

import com.r2s.core.config.SecurityConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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

        String authHeader = req.getHeader(SecurityConstants.HEADER_STRING);

        // [?] 1. No header or the Bearer format is not correct, continue
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return; // [?] Finish processing this Filter here
        }

        // [?] 2. Extract Token (remove the word "Bearer")
        String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();

        try {

            // [?] 3. get username from token
            final String username = jwtUtil.extractUsername(token);

            // [?] 4. If the username is present and not authenticated in this session (SecurityContext)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {


                // [?] 5. Validate token and set authentication
                if (jwtUtil.isTokenValid(token)) {

                    // [?] Find user in Database
                    UserDetails userDetails = userDetailService.loadUserByUsername(username);

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
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            req.setAttribute("jwt_error", "Token has expired. Please refresh.");
        } catch (SignatureException | MalformedJwtException e) {
            log.error("Security violation - Invalid JWT: {}", e.getMessage());
            req.setAttribute("jwt_error", "Invalid token integrity.");
        } catch (Exception e) {
            log.error("Authentication failed: ", e);
            req.setAttribute("jwt_error", "Authentication failed.");
        }

        // [?] 6. Always call chain.doFilter to request to continue the journey
        chain.doFilter(req, res);
    }
}
