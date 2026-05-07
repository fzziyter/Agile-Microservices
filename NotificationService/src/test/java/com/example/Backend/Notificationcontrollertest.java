package com.example.Backend;

import com.example.Backend.controller.GlobalExceptionHandler;
import com.example.Backend.controller.NotificationController;
import com.example.Backend.model.Notification;
import com.example.Backend.model.NotificationType;
import com.example.Backend.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("NotificationController – Integration Tests (MockMvc)")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setRecipientId(10L);
        notification.setRecipientEmail("user@example.com");
        notification.setMessage("You have been assigned a task.");
        notification.setType(NotificationType.ASSIGNMENT);
        notification.setReadFlag(false);
        notification.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    // ─── POST /api/notifications ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/notifications – 201 with created body")
    void create_shouldReturn201WithCreatedNotification() throws Exception {
        when(notificationService.create(any(Notification.class))).thenReturn(notification);

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("You have been assigned a task."))
                .andExpect(jsonPath("$.type").value("ASSIGNMENT"))
                .andExpect(jsonPath("$.readFlag").value(false));
    }

    // ─── GET /api/notifications ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/notifications – 200 with list of notifications")
    void listAll_shouldReturn200WithList() throws Exception {
        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setMessage("Invitation received.");
        n2.setType(NotificationType.INVITATION);
        n2.setCreatedAt(LocalDateTime.now());

        when(notificationService.findAll()).thenReturn(List.of(notification, n2));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/notifications – 200 with empty list")
    void listAll_shouldReturn200WithEmptyList() throws Exception {
        when(notificationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/notifications/user/{userId} ─────────────────────────────

    @Test
    @DisplayName("GET /api/notifications/user/{userId} – 200 with user's notifications")
    void getByUser_shouldReturn200WithUserNotifications() throws Exception {
        when(notificationService.findByUser(10L)).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipientId").value(10));
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId} – 200 empty for unknown user")
    void getByUser_shouldReturn200EmptyForUnknownUser() throws Exception {
        when(notificationService.findByUser(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/notifications/email/{email} ─────────────────────────────

    @Test
    @DisplayName("GET /api/notifications/email/{email} – 200 with matching notifications")
    void getByEmail_shouldReturn200WithEmailNotifications() throws Exception {
        when(notificationService.findByEmail("user@example.com")).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/email/user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipientEmail").value("user@example.com"));
    }

    // ─── PATCH /api/notifications/{id}/read ──────────────────────────────

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read – 200 with readFlag=true")
    void markAsRead_shouldReturn200WithUpdatedNotification() throws Exception {
        notification.setReadFlag(true);
        when(notificationService.markAsRead(1L)).thenReturn(notification);

        mockMvc.perform(patch("/api/notifications/1/read").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.readFlag").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/notifications/{id}/read returns 500 when notification not found")
    void markAsRead_shouldReturn500WhenNotFound() throws Exception {
        when(notificationService.markAsRead(99L))
                .thenThrow(new RuntimeException("Notification non trouvee"));

        mockMvc.perform(patch("/api/notifications/99/read")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Notification non trouvee"));
    }

    // ─── DELETE /api/notifications/{id} ──────────────────────────────────

    @Test
    @DisplayName("DELETE /api/notifications/{id} – 204 no content")
    void delete_shouldReturn204() throws Exception {
        doNothing().when(notificationService).delete(1L);

        mockMvc.perform(delete("/api/notifications/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).delete(1L);
    }
}