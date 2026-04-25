package com.example.Backend.security;

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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // DEBUG — remove after fix is confirmed
        System.out.println(">>> JWT Filter hit: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println(">>> Authorization header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role     = jwtUtil.extractRole(token);

                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                System.out.println(">>> Token valid. User=" + username + " Authority=" + authority);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println(">>> Token INVALID or EXPIRED");
            }
        } else {
            System.out.println(">>> No Bearer token found — anonymous request");
        }

        filterChain.doFilter(request, response);
    }
}