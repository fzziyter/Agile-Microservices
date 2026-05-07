package com.example.Backend;
import com.example.Backend.controller.GlobalExceptionHandler;

import com.example.Backend.controller.BacklogController;
import com.example.Backend.dto.StatusUpdateRequest;
import com.example.Backend.dto.TaskSprintDTO;
import com.example.Backend.model.BacklogItem;
import com.example.Backend.model.ItemType;
import com.example.Backend.model.TaskStatus;
import com.example.Backend.service.BacklogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BacklogController.class)
@Import(GlobalExceptionHandler.class)  // ← pulls the handler into the WebMvcTest slice
class BacklogControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean BacklogService backlogService;

    // ─────────────────────────────────────────────────────────────
    // POST /{projectId}  — create item
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PRODUCT_OWNER")
    @DisplayName("POST /api/backlog/{projectId} returns 201 with created item")
    void create_returns201() throws Exception {
        BacklogItem saved = backlogItem(10L, "Login page", TaskStatus.TODO, 1L);
        when(backlogService.addItem(eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post("/api/backlog/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Login page"));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_OWNER")
    @DisplayName("POST /api/backlog/{projectId} returns 500 when project not found")
    void create_projectNotFound_returns500() throws Exception {
        when(backlogService.addItem(eq(1L), any()))
                .thenThrow(new RuntimeException("Projet non trouve dans MS-PROJECTS"));

        mockMvc.perform(post("/api/backlog/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Projet non trouve dans MS-PROJECTS"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /project/{projectId}
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /api/backlog/project/{projectId} returns list of items")
    void getByProject_returnsList() throws Exception {
        when(backlogService.getItemsByProject(2L)).thenReturn(List.of(
                backlogItem(1L, "Task A", TaskStatus.TODO, 2L),
                backlogItem(2L, "Task B", TaskStatus.IN_PROGRESS, 2L)
        ));

        mockMvc.perform(get("/api/backlog/project/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Task A"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /api/backlog/project/{projectId} returns empty list")
    void getByProject_emptyList() throws Exception {
        when(backlogService.getItemsByProject(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/backlog/project/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /sprint/{sprintId}
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SCRUM_MASTER")
    @DisplayName("GET /api/backlog/sprint/{sprintId} returns items for sprint")
    void getBySprint_returnsList() throws Exception {
        when(backlogService.getItemsBySprint(3L)).thenReturn(List.of(
                backlogItem(5L, "Sprint task", TaskStatus.TODO, 1L)
        ));

        mockMvc.perform(get("/api/backlog/sprint/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /{itemId} — full update
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PUT /api/backlog/{itemId} returns updated item")
    void update_returnsUpdatedItem() throws Exception {
        BacklogItem updated = backlogItem(1L, "Updated title", TaskStatus.IN_PROGRESS, 1L);
        when(backlogService.updateItem(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/backlog/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /{itemId}/status
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PATCH /api/backlog/{itemId}/status updates status to BLOCKED with comment")
    void updateStatus_blocked() throws Exception {
        BacklogItem result = backlogItem(1L, "Task", TaskStatus.BLOCKED, 1L);
        result.setBlockedComment("Waiting for review");
        when(backlogService.updateStatus(eq(1L), eq(TaskStatus.BLOCKED), eq("Waiting for review")))
                .thenReturn(result);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.BLOCKED);
        req.setComment("Waiting for review");

        mockMvc.perform(patch("/api/backlog/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.blockedComment").value("Waiting for review"));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PATCH /api/backlog/{itemId}/status updates status to DONE")
    void updateStatus_done() throws Exception {
        BacklogItem result = backlogItem(2L, "Task", TaskStatus.DONE, 1L);
        when(backlogService.updateStatus(eq(2L), eq(TaskStatus.DONE), isNull()))
                .thenReturn(result);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.DONE);

        mockMvc.perform(patch("/api/backlog/2/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /{itemId}/statut  (French alias)
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PATCH /api/backlog/{itemId}/statut (alias) works identically")
    void updateStatut_alias() throws Exception {
        BacklogItem result = backlogItem(1L, "T", TaskStatus.IN_PROGRESS, 1L);
        when(backlogService.updateStatus(eq(1L), eq(TaskStatus.IN_PROGRESS), isNull()))
                .thenReturn(result);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/backlog/1/statut")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /{itemId}/sprint-info
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SCRUM_MASTER")
    @DisplayName("GET /api/backlog/{itemId}/sprint-info returns TaskSprintDTO")
    void getSprintInfo_returnsDtoWithSprintData() throws Exception {
        TaskSprintDTO dto = new TaskSprintDTO(1L, "Task A", 8.0, 3L, 16.0);
        when(backlogService.getTaskWithSprintInfo(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/backlog/1/sprint-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.title").value("Task A"))
                .andExpect(jsonPath("$.sprintId").value(3))
                .andExpect(jsonPath("$.remainingCapacityHours").value(16.0));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /api/backlog/{itemId}/sprint-info returns null sprint fields when no sprint")
    void getSprintInfo_noSprint_nullFields() throws Exception {
        TaskSprintDTO dto = new TaskSprintDTO(2L, "Unassigned", 3.0, null, null);
        when(backlogService.getTaskWithSprintInfo(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/backlog/2/sprint-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintId").doesNotExist());
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /{itemId}
    // ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PRODUCT_OWNER")
    @DisplayName("DELETE /api/backlog/{itemId} returns 204")
    void delete_returns204() throws Exception {
        doNothing().when(backlogService).deleteItem(7L);

        mockMvc.perform(delete("/api/backlog/7").with(csrf()))
                .andExpect(status().isNoContent());

        verify(backlogService).deleteItem(7L);
    }

    // ─────────────────────────────────────────────────────────────
    // Security
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated requests return 401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/backlog/project/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    private BacklogItem backlogItem(Long id, String title, TaskStatus status, Long projectId) {
        BacklogItem i = new BacklogItem();
        i.setId(id);
        i.setTitle(title);
        i.setStatus(status);
        i.setProjectId(projectId);
        i.setType(ItemType.USER_STORY);
        return i;
    }
}