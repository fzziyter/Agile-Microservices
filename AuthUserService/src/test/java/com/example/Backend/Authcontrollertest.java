package com.example.Backend;

import com.example.Backend.controller.AuthController;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    // -----------------------------------------------------------------------
    // Minimal security config: login is public, /me requires authentication.
    // No JWT filter needed — @WithMockUser populates SecurityContext directly.
    // -----------------------------------------------------------------------
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/me").authenticated()
                    .anyRequest().permitAll()
                )
                .sessionManagement(sess ->
                    sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex                                          // ← add this
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(401, "Unauthorized"))
                );
            return http.build();
    }
}

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserRepository userRepository;
    @MockitoBean JwtUtil jwtUtil;

    // -----------------------------------------------------------------------
    // POST /api/auth/login — success
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login returns token")
    void login_validCredentials_returns200WithToken() throws Exception {
        User user = buildUser(1L, "alice", Role.DEVELOPER);

        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("alice", "DEVELOPER")).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.id").value(1));
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login — bad credentials
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login returns 401 on bad credentials")
    void login_invalidCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // -----------------------------------------------------------------------
    // GET /api/auth/me — authenticated user found in DB
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("me returns user when authenticated")
    @WithMockUser(username = "bob", roles = "SCRUM_MASTER") // ← key fix
    void me_authenticated_returnsUserInfo() throws Exception {
        User user = buildUser(2L, "bob", Role.SCRUM_MASTER);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.role").value("SCRUM_MASTER"))
                .andExpect(jsonPath("$.id").value(2));
    }

    // -----------------------------------------------------------------------
    // GET /api/auth/me — no authentication at all
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("me returns 401 when not authenticated")
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // GET /api/auth/me — authenticated but user missing from DB
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("me returns 404 when user not in DB")
    @WithMockUser(username = "ghost", roles = "DEVELOPER") // ← key fix
    void me_userNotInDb_returns404() throws Exception {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------

    private User buildUser(Long id, String username, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRole(role);
        u.setPassword("hashed");
        return u;
    }
}