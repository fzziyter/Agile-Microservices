package com.example.Backend.config;

import com.example.Backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(csrf -> csrf.disable())

                // ✅ FIXED: use the CorsConfigurationSource bean below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // JWT is stateless: no server-side session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public: Swagger docs and the login endpoint
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // 1. User management: ADMIN only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 2. Creating projects: ADMIN or PRODUCT_OWNER
                        // ✅ FIXED: "/api/projects/**" alone does NOT match POST /api/projects
                        .requestMatchers(HttpMethod.POST, "/api/projects", "/api/projects/**")
                        .hasAnyRole("ADMIN", "PRODUCT_OWNER")

                        // 3. Backlog management: PRODUCT_OWNER or ADMIN
                        // ✅ FIXED: added ADMIN
                        .requestMatchers("/api/backlog/**").hasAnyRole("PRODUCT_OWNER", "ADMIN","DEVELOPER")

                        // 4. Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Register our JWT filter BEFORE Spring's default UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ FIXED: Proper CorsConfigurationSource bean.
     *
     * The old code used .cors(cors -> cors.configure(http)) which is broken in
     * Spring Boot 3 — it calls an internal no-op method and registers NO CORS rules.
     * Without this, Spring Security rejects preflight OPTIONS requests with 403
     * before the JWT filter even runs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow your frontend origins (adjust ports if needed)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React dev server
                "http://localhost:5173"    // Vite dev server
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Exposes the AuthenticationManager so AuthController can authenticate credentials. */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}