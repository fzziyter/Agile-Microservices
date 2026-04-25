package com.example.Backend.controller;

import com.example.Backend.model.User;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/auth/login
     *
     * Body: { "username": "...", "password": "..." }
     *
     * Returns: { "token": "<JWT>", "username": "...", "role": "..." }
     * or 401 if credentials are invalid.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            // Let Spring Security verify the credentials against the database
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Credentials are valid — fetch the user to get the role
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow();

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            return ResponseEntity.ok(Map.of(
                    "token",    token,
                    "username", user.getUsername(),
                    "role",     user.getRole().name()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password."));
        }
    }

    /**
     * GET /api/auth/me
     *
     * Returns the currently authenticated user's info.
     * Requires a valid Bearer token.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated."));
        }

        return userRepository.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(Map.of(
                        "username", user.getUsername(),
                        "role",     user.getRole().name()
                )))
                .orElse(ResponseEntity.status(404).build());
    }
}
