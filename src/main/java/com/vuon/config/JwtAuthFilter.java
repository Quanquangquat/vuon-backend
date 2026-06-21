package com.vuon.config;

import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filter JWT: chạy một lần mỗi request
 * Đọc Authorization header → verify token → set Authentication vào SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil          jwtUtil;
    private final UserRepository   userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.isValid(token)) {
                UUID userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // Tạo Authentication object và set vào SecurityContext
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,                                             // principal
                        null,                                               // credentials
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
