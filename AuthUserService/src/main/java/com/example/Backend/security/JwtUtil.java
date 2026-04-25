package com.example.Backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        // Key must be at least 256 bits for HS256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Generate a signed JWT containing the username and role. */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    /** Parse and validate the token; returns all claims. */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extract the username (subject) from a token. */
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    /** Extract the role claim from a token. */
    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /** Returns true if the token signature is valid and it has not expired. */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token); // throws if invalid or expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
