package com.example.Backend;

import com.example.Backend.controller.AdminController;
import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.security.JwtAuthenticationFilter;
import com.example.Backend.security.JwtUtil;
import com.example.Backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.TestSecurityConfig.class)
class AdminControllerTest {

    // -----------------------------------------------------------------------
    // Minimal security config: enforces ADMIN role on /api/admin/**
    // No JWT filter — .with(user(...)) injects auth directly into SecurityContext
    // -----------------------------------------------------------------------
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
                )
                .sessionManagement(sess ->
                    sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // JwtUtil must be mocked so Spring doesn't complain about missing bean
    // even though the JWT filter is not in our TestSecurityConfig
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean UserService userService;

    // -----------------------------------------------------------------------
    // POST /api/admin/users
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/admin/users returns 201 with created user (ADMIN)")
    void createUser_asAdmin_returns201() throws Exception {
        User incoming = buildUser(null, "carol", "pass", Role.DEVELOPER);
        User saved    = buildUser(10L, "carol", "hashed", Role.DEVELOPER);

        when(userService.createUser(any())).thenReturn(saved);

        mockMvc.perform(post("/api/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("carol"));
    }

    @Test
    @DisplayName("POST /api/admin/users returns 403 for non-ADMIN")
    void createUser_nonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("dev").roles("DEVELOPER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/users
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/admin/users returns list (ADMIN)")
    void getAllUsers_asAdmin_returnsList() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(
                buildUser(1L, "alice", null, Role.ADMIN),
                buildUser(2L, "bob",   null, Role.DEVELOPER)
        ));

        mockMvc.perform(get("/api/admin/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    @DisplayName("GET /api/admin/users returns 403 for DEVELOPER")
    void getAllUsers_asDeveloper_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(user("dev").roles("DEVELOPER")))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/admin/users/{id}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE returns 204 (ADMIN)")
    void deleteUser_asAdmin_returns204() throws Exception {
        doNothing().when(userService).deleteUser(5L);

        mockMvc.perform(delete("/api/admin/users/5")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(5L);
    }

    @Test
    @DisplayName("DELETE returns 403 for non-ADMIN")
    void deleteUser_nonAdmin_returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/users/5")
                        .with(user("po").roles("PRODUCT_OWNER")))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // PUT /api/admin/users/{id}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PUT returns updated user (ADMIN)")
    void updateUser_asAdmin_returnsUpdated() throws Exception {
        User patch   = buildUser(null, "alice_v2", null, Role.SCRUM_MASTER);
        User updated = buildUser(1L,   "alice_v2", "h",  Role.SCRUM_MASTER);

        when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/admin/users/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_v2"))
                .andExpect(jsonPath("$.role").value("SCRUM_MASTER"));
    }

    // -----------------------------------------------------------------------

    private User buildUser(Long id, String username, String password, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }
}