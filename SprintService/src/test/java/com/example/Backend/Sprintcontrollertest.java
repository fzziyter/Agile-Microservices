package com.example.Backend;

import com.example.Backend.controller.SprintController;
import com.example.Backend.model.Sprint;
import com.example.Backend.model.SprintStatus;
import com.example.Backend.service.SprintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SprintController.class)
@WithMockUser
@DisplayName("SprintController – Integration Tests (MockMvc)")
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SprintService sprintService;

    private ObjectMapper objectMapper;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sprint = new Sprint();
        sprint.setId(1L);
        sprint.setName("Sprint 1");
        sprint.setGoal("Deliver authentication module");
        sprint.setStartDate(LocalDate.of(2025, 1, 1));
        sprint.setEndDate(LocalDate.of(2025, 1, 14));
        sprint.setCapacityHours(80.0);
        sprint.setRemainingCapacityHours(80.0);
        sprint.setStatus(SprintStatus.PLANNED);
        sprint.setProjectId(100L);
    }

    // ─── POST /api/sprints ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/sprints – 201 with created sprint")
    void create_shouldReturn201() throws Exception {
        when(sprintService.create(any(Sprint.class))).thenReturn(sprint);

        mockMvc.perform(post("/api/sprints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sprint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.projectId").value(100));
    }

    // ─── GET /api/sprints ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/sprints – 200 with list of sprints")
    void listAll_shouldReturn200WithList() throws Exception {
        Sprint s2 = new Sprint();
        s2.setId(2L);
        s2.setName("Sprint 2");
        s2.setStatus(SprintStatus.IN_PROGRESS);

        when(sprintService.findAll()).thenReturn(List.of(sprint, s2));

        mockMvc.perform(get("/api/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/sprints – 200 with empty list")
    void listAll_shouldReturn200WhenEmpty() throws Exception {
        when(sprintService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/sprints/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/sprints/{id} – 200 with sprint")
    void getById_shouldReturn200() throws Exception {
        when(sprintService.findById(1L)).thenReturn(sprint);

        mockMvc.perform(get("/api/sprints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.goal").value("Deliver authentication module"));
    }

    @Test
    @DisplayName("GET /api/sprints/{id} – 500 when sprint not found")
    void getById_shouldReturn500WhenNotFound() throws Exception {
        when(sprintService.findById(999L)).thenThrow(new RuntimeException("Sprint non trouve avec l'id : 999"));

        mockMvc.perform(get("/api/sprints/999"))
                .andExpect(status().is5xxServerError());
    }

    // ─── GET /api/sprints/project/{projectId} ────────────────────────────

    @Test
    @DisplayName("GET /api/sprints/project/{projectId} – 200 with project sprints")
    void getByProject_shouldReturn200() throws Exception {
        when(sprintService.findByProject(100L)).thenReturn(List.of(sprint));

        mockMvc.perform(get("/api/sprints/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(100));
    }

    @Test
    @DisplayName("GET /api/sprints/project/{projectId} – 200 empty for unknown project")
    void getByProject_shouldReturn200EmptyForUnknownProject() throws Exception {
        when(sprintService.findByProject(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/sprints/project/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/sprints/projet/{projectId} (alias) ─────────────────────

    @Test
    @DisplayName("GET /api/sprints/projet/{projectId} – alias delegates to same service method")
    void getByProjet_shouldDelegateToGetByProject() throws Exception {
        when(sprintService.findByProject(100L)).thenReturn(List.of(sprint));

        mockMvc.perform(get("/api/sprints/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/sprints/projet/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(sprintService, times(2)).findByProject(100L);
    }

    // ─── PUT /api/sprints/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/sprints/{id} – 200 with updated sprint")
    void update_shouldReturn200WithUpdatedSprint() throws Exception {
        Sprint updated = new Sprint();
        updated.setId(1L);
        updated.setName("Sprint 1 Updated");
        updated.setStatus(SprintStatus.IN_PROGRESS);
        updated.setCapacityHours(100.0);
        updated.setRemainingCapacityHours(90.0);
        updated.setProjectId(100L);

        when(sprintService.update(eq(1L), any(Sprint.class))).thenReturn(updated);

        mockMvc.perform(put("/api/sprints/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1 Updated"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.capacityHours").value(100.0));
    }

    @Test
    @DisplayName("PUT /api/sprints/{id} – 500 when sprint not found")
    void update_shouldReturn500WhenNotFound() throws Exception {
        when(sprintService.update(eq(999L), any()))
                .thenThrow(new RuntimeException("Sprint non trouve avec l'id : 999"));

        mockMvc.perform(put("/api/sprints/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sprint)))
                .andExpect(status().is5xxServerError());
    }

    // ─── DELETE /api/sprints/{id} ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/sprints/{id} – 204 no content")
    void delete_shouldReturn204() throws Exception {
        doNothing().when(sprintService).delete(1L);

        mockMvc.perform(delete("/api/sprints/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(sprintService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/sprints/{id} – 500 when sprint not found")
    void delete_shouldReturn500WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Sprint non trouve avec l'id : 999"))
                .when(sprintService).delete(999L);

        mockMvc.perform(delete("/api/sprints/999").with(csrf()))
                .andExpect(status().is5xxServerError());
    }
}