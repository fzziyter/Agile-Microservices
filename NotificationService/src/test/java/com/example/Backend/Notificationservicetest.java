package com.example.Backend;

import com.example.Backend.model.Notification;
import com.example.Backend.model.NotificationType;
import com.example.Backend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.Backend.service.NotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService – Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

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
        notification.setCreatedAt(LocalDateTime.now());
    }

    // ─── create ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() – persists and returns the notification")
    void create_shouldSaveAndReturnNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.create(notification);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("You have been assigned a task.");
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("create() – sets type correctly")
    void create_shouldPreserveType() {
        notification.setType(NotificationType.STATUS_CHANGE);
        when(notificationRepository.save(any())).thenReturn(notification);

        Notification result = notificationService.create(notification);

        assertThat(result.getType()).isEqualTo(NotificationType.STATUS_CHANGE);
    }

    // ─── findAll ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() – returns all notifications")
    void findAll_shouldReturnAllNotifications() {
        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setMessage("Invitation received.");
        n2.setType(NotificationType.INVITATION);

        when(notificationRepository.findAll()).thenReturn(List.of(notification, n2));

        List<Notification> result = notificationService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("findAll() – returns empty list when no notifications exist")
    void findAll_shouldReturnEmptyList() {
        when(notificationRepository.findAll()).thenReturn(List.of());

        List<Notification> result = notificationService.findAll();

        assertThat(result).isEmpty();
    }

    // ─── findByUser ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUser() – returns notifications for the given userId")
    void findByUser_shouldReturnNotificationsForUser() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.findByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipientId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findByUser() – returns empty list when user has no notifications")
    void findByUser_shouldReturnEmptyListForUnknownUser() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(999L))
                .thenReturn(List.of());

        List<Notification> result = notificationService.findByUser(999L);

        assertThat(result).isEmpty();
    }

    // ─── findByEmail ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByEmail() – returns notifications for the given email")
    void findByEmail_shouldReturnNotificationsForEmail() {
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.findByEmail("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipientEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("findByEmail() – returns empty list for unknown email")
    void findByEmail_shouldReturnEmptyListForUnknownEmail() {
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc("nobody@example.com"))
                .thenReturn(List.of());

        List<Notification> result = notificationService.findByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    // ─── markAsRead ───────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead() – marks notification as read and saves it")
    void markAsRead_shouldSetReadFlagAndSave() {
        notification.setReadFlag(false);
        Notification saved = new Notification();
        saved.setId(1L);
        saved.setReadFlag(true);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(saved);

        Notification result = notificationService.markAsRead(1L);

        assertThat(result.isReadFlag()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead() – throws RuntimeException when notification not found")
    void markAsRead_shouldThrowWhenNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification non trouvee");
    }

    // ─── delete ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() – calls repository deleteById once")
    void delete_shouldInvokeDeleteById() {
        doNothing().when(notificationRepository).deleteById(1L);

        notificationService.delete(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }
}