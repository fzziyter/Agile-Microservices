package com.example.Backend;

import com.example.Backend.client.NotificationClient;
import com.example.Backend.client.ProjectClient;
import com.example.Backend.client.SprintClient;
import com.example.Backend.dto.ProjectDTO;
import com.example.Backend.dto.SprintDTO;
import com.example.Backend.dto.TaskSprintDTO;
import com.example.Backend.model.BacklogItem;
import com.example.Backend.model.ItemType;
import com.example.Backend.model.TaskStatus;
import com.example.Backend.repository.BacklogItemRepository;
import com.example.Backend.service.BacklogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BacklogServiceTest {

    @Mock BacklogItemRepository backlogRepository;
    @Mock ProjectClient        projectClient;
    @Mock SprintClient         sprintClient;
    @Mock NotificationClient   notificationClient;

    @InjectMocks BacklogService backlogService;

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private BacklogItem item(Long id, String title, TaskStatus status) {
        BacklogItem i = new BacklogItem();
        i.setId(id);
        i.setTitle(title);
        i.setStatus(status);
        return i;
    }

    private ProjectDTO project(Long id) {
        ProjectDTO p = new ProjectDTO();
        p.setId(id);
        p.setName("Project-" + id);
        return p;
    }

    private SprintDTO sprint(Long id, double remaining) {
        SprintDTO s = new SprintDTO();
        s.setId(id);
        s.setRemainingCapacityHours(remaining);
        return s;
    }

    // ─────────────────────────────────────────────────────────────
    // addItem
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("saves item and sets projectId when project exists")
        void savesItemAndSetsProjectId() {
            when(projectClient.getProjectById(1L)).thenReturn(project(1L));
            BacklogItem incoming = item(null, "Login page", TaskStatus.TODO);
            BacklogItem saved    = item(10L, "Login page", TaskStatus.TODO);
            saved.setProjectId(1L);
            when(backlogRepository.save(any())).thenReturn(saved);

            BacklogItem result = backlogService.addItem(1L, incoming);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(incoming.getProjectId()).isEqualTo(1L);
            verify(backlogRepository).save(incoming);
        }

        @Test
        @DisplayName("throws RuntimeException when project not found")
        void throwsWhenProjectNotFound() {
            when(projectClient.getProjectById(99L)).thenReturn(null);
            assertThatThrownBy(() -> backlogService.addItem(99L, new BacklogItem()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Projet non trouve");
        }

        @Test
        @DisplayName("sends assignment notification when assignedToId is set")
        void sendsNotificationWhenAssigned() {
            BacklogItem saved = item(1L, "Task A", TaskStatus.TODO);
            saved.setProjectId(1L);
            saved.setAssignedToId(42L);

            when(projectClient.getProjectById(1L)).thenReturn(project(1L));
            when(backlogRepository.save(any())).thenReturn(saved);

            backlogService.addItem(1L, saved);

            verify(notificationClient).create(argThat(n ->
                    n.getRecipientId().equals(42L) && "ASSIGNMENT".equals(n.getType())));
        }

        @Test
        @DisplayName("skips notification when assignedToId is null")
        void skipsNotificationWhenNoAssignee() {
            BacklogItem saved = item(1L, "Unassigned", TaskStatus.TODO);
            saved.setProjectId(1L);

            when(projectClient.getProjectById(1L)).thenReturn(project(1L));
            when(backlogRepository.save(any())).thenReturn(saved);

            backlogService.addItem(1L, saved);

            verify(notificationClient, never()).create(any());
        }

        @Test
        @DisplayName("does not propagate exception when notification client fails")
        void doesNotPropagateNotificationFailure() {
            BacklogItem saved = item(1L, "Task X", TaskStatus.TODO);
            saved.setProjectId(1L);
            saved.setAssignedToId(5L);

            when(projectClient.getProjectById(1L)).thenReturn(project(1L));
            when(backlogRepository.save(any())).thenReturn(saved);
            doThrow(new RuntimeException("Notification service down"))
                    .when(notificationClient).create(any());

            assertThatNoException().isThrownBy(() -> backlogService.addItem(1L, saved));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getItemsByProject / getItemsBySprint
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        @DisplayName("getItemsByProject delegates to repository")
        void byProject() {
            List<BacklogItem> list = List.of(item(1L, "A", TaskStatus.TODO));
            when(backlogRepository.findByProjectId(3L)).thenReturn(list);
            assertThat(backlogService.getItemsByProject(3L)).isEqualTo(list);
        }

        @Test
        @DisplayName("getItemsBySprint delegates to repository")
        void bySprint() {
            List<BacklogItem> list = List.of(item(2L, "B", TaskStatus.IN_PROGRESS));
            when(backlogRepository.findBySprintId(7L)).thenReturn(list);
            assertThat(backlogService.getItemsBySprint(7L)).isEqualTo(list);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // updateItem
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("applies all non-null fields from patch")
        void appliesNonNullFields() {
            BacklogItem existing = item(1L, "Old title", TaskStatus.TODO);
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BacklogItem patch = new BacklogItem();
            patch.setTitle("New title");
            patch.setPriority("HIGH");
            patch.setType(ItemType.BUG);
            patch.setStatus(TaskStatus.IN_PROGRESS);
            patch.setEstimatedHours(8.0);
            patch.setStoryPoints(5);

            BacklogItem result = backlogService.updateItem(1L, patch);

            assertThat(result.getTitle()).isEqualTo("New title");
            assertThat(result.getPriority()).isEqualTo("HIGH");
            assertThat(result.getType()).isEqualTo(ItemType.BUG);
            assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(result.getEstimatedHours()).isEqualTo(8.0);
            assertThat(result.getStoryPoints()).isEqualTo(5);
        }

        @Test
        @DisplayName("skips null fields (partial update)")
        void skipsNullFields() {
            BacklogItem existing = item(1L, "Keep me", TaskStatus.TODO);
            existing.setPriority("LOW");
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BacklogItem result = backlogService.updateItem(1L, new BacklogItem());

            assertThat(result.getTitle()).isEqualTo("Keep me");
            assertThat(result.getPriority()).isEqualTo("LOW");
        }

        @Test
        @DisplayName("throws RuntimeException when item not found")
        void throwsWhenNotFound() {
            when(backlogRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> backlogService.updateItem(99L, new BacklogItem()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Item non trouve");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // updateStatus
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("sets BLOCKED status and stores comment")
        void blockedSetsComment() {
            BacklogItem existing = item(1L, "Blocker", TaskStatus.IN_PROGRESS);
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BacklogItem result = backlogService.updateStatus(1L, TaskStatus.BLOCKED, "Waiting for API");

            assertThat(result.getStatus()).isEqualTo(TaskStatus.BLOCKED);
            assertThat(result.getBlockedComment()).isEqualTo("Waiting for API");
        }

        @Test
        @DisplayName("IN_PROGRESS clears blockedComment")
        void inProgressClearsBlockedComment() {
            BacklogItem existing = item(1L, "Task", TaskStatus.BLOCKED);
            existing.setBlockedComment("Old block");
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BacklogItem result = backlogService.updateStatus(1L, TaskStatus.IN_PROGRESS, null);

            assertThat(result.getBlockedComment()).isNull();
        }

        @Test
        @DisplayName("DONE clears blockedComment")
        void doneClearsBlockedComment() {
            BacklogItem existing = item(1L, "Task", TaskStatus.BLOCKED);
            existing.setBlockedComment("Still blocked");
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            BacklogItem result = backlogService.updateStatus(1L, TaskStatus.DONE, null);
            assertThat(result.getBlockedComment()).isNull();
        }

        @Test
        @DisplayName("sends STATUS_CHANGE notification when assignee present")
        void sendsStatusChangeNotification() {
            BacklogItem existing = item(1L, "Task", TaskStatus.TODO);
            existing.setAssignedToId(7L);
            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(backlogRepository.save(any())).thenReturn(existing);

            backlogService.updateStatus(1L, TaskStatus.IN_PROGRESS, null);

            verify(notificationClient).create(argThat(n ->
                    "STATUS_CHANGE".equals(n.getType()) && n.getRecipientId().equals(7L)));
        }

        @Test
        @DisplayName("throws when item not found")
        void throwsWhenNotFound() {
            when(backlogRepository.findById(404L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> backlogService.updateStatus(404L, TaskStatus.DONE, null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getTaskWithSprintInfo
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTaskWithSprintInfo")
    class GetTaskWithSprintInfo {

        @Test
        @DisplayName("returns TaskSprintDTO with sprint info when sprintId is set")
        void withSprintId() {
            BacklogItem existing = item(1L, "Task A", TaskStatus.IN_PROGRESS);
            existing.setSprintId(5L);
            existing.setEstimatedHours(4.0);

            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(sprintClient.getSprintById(5L)).thenReturn(sprint(5L, 20.0));

            TaskSprintDTO dto = backlogService.getTaskWithSprintInfo(1L);

            assertThat(dto.getTaskId()).isEqualTo(1L);
            assertThat(dto.getTitle()).isEqualTo("Task A");
            assertThat(dto.getSprintId()).isEqualTo(5L);
            assertThat(dto.getRemainingCapacityHours()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("returns TaskSprintDTO with null sprint fields when no sprintId")
        void withoutSprintId() {
            BacklogItem existing = item(1L, "Task B", TaskStatus.TODO);
            existing.setEstimatedHours(2.0);

            when(backlogRepository.findById(1L)).thenReturn(Optional.of(existing));

            TaskSprintDTO dto = backlogService.getTaskWithSprintInfo(1L);

            assertThat(dto.getSprintId()).isNull();
            assertThat(dto.getRemainingCapacityHours()).isNull();
            verify(sprintClient, never()).getSprintById(any());
        }

        @Test
        @DisplayName("throws when item not found")
        void throwsWhenNotFound() {
            when(backlogRepository.findById(55L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> backlogService.getTaskWithSprintInfo(55L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // deleteItem
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteItem delegates to repository.deleteById")
    void deleteItem_callsRepository() {
        backlogService.deleteItem(3L);
        verify(backlogRepository).deleteById(3L);
    }
}