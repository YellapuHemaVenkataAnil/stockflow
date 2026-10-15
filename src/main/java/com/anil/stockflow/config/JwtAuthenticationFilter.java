
package com.anil.stockflow.config;

import com.anil.stockflow.entity.User;
import com.anil.stockflow.repository.UserRepository;
import com.anil.stockflow.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String username = jwtService.extractUsername(token);

                if (!jwtService.isTokenValid(token, username)) {
                    SecurityContextHolder.clearContext();
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid or expired JWT token");
                    return;
                }

                Optional<User> userOptional =
                        userRepository.findByUsername(username);

                if (userOptional.isEmpty()) {
                    SecurityContextHolder.clearContext();
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User associated with token no longer exists");
                    return;
                }

                User user = userOptional.get();
                String role = user.getRole();

                if (role == null || role.isBlank()) {
                    SecurityContextHolder.clearContext();
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "User role is not configured");
                    return;
                }

                if (SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

                    String authority = role.startsWith("ROLE_")
                            ? role
                            : "ROLE_" + role;

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority(authority)));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (io.jsonwebtoken.JwtException
                     | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired JWT token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
