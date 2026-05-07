package com.example.Backend;

import com.example.Backend.controller.ProjectController;
import com.example.Backend.dto.InviteMemberRequest;
import com.example.Backend.model.Project;
import com.example.Backend.model.ProjectMember;
import com.example.Backend.model.ProjectRole;
import com.example.Backend.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Security is excluded in BackendApplication for ProjectService
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProjectService projectService;

    // ─────────────────────────────────────────────────────────
    // POST /api/projects
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/projects returns 201 with created project")
    void create_returns201() throws Exception {
        Project saved = project(1L, "My Project");
        when(projectService.createProject(any())).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project(null, "My Project"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Project"));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/projects
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/projects returns list of all projects")
    void listAll_returnsList() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(
                project(1L, "P1"), project(2L, "P2")
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("P1"))
                .andExpect(jsonPath("$[1].name").value("P2"));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/projects/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/projects/{id} returns project when found")
    void getById_returnsProject() throws Exception {
        when(projectService.findById(1L)).thenReturn(project(1L, "Found"));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Found"));
    }

    @Test
    @DisplayName("GET /api/projects/{id} returns 500 when not found")
    void getById_notFound_returns500() throws Exception {
        when(projectService.findById(99L)).thenThrow(new RuntimeException("Projet non trouvé"));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────────────────
    // PUT /api/projects/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/projects/{id} returns updated project")
    void update_returnsUpdated() throws Exception {
        Project updated = project(1L, "Updated");
        when(projectService.updateProject(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // ─────────────────────────────────────────────────────────
    // DELETE /api/projects/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/projects/{id} returns 204")
    void delete_returns204() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(1L);
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/projects/{id}/members
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/projects/{id}/members returns 201 with saved member")
    void inviteMember_returns201() throws Exception {
        ProjectMember saved = member(5L, 1L, "alice@x.com", ProjectRole.DEV);
        when(projectService.inviteMember(eq(1L), any())).thenReturn(saved);

        InviteMemberRequest req = new InviteMemberRequest();
        req.setEmail("alice@x.com");
        req.setRole(ProjectRole.DEV);

        mockMvc.perform(post("/api/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@x.com"))
                .andExpect(jsonPath("$.role").value("DEV"));
    }

    @Test
    @DisplayName("POST /api/projects/{id}/membres (alias) delegates to inviteMember")
    void inviteMembre_alias_delegates() throws Exception {
        ProjectMember saved = member(6L, 1L, "bob@x.com", ProjectRole.QA);
        when(projectService.inviteMember(eq(1L), any())).thenReturn(saved);

        InviteMemberRequest req = new InviteMemberRequest();
        req.setEmail("bob@x.com");
        req.setRole(ProjectRole.QA);

        mockMvc.perform(post("/api/projects/1/membres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob@x.com"));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/projects/{id}/members
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/projects/{id}/members returns members list")
    void getMembers_returnsList() throws Exception {
        when(projectService.getMembers(1L)).thenReturn(List.of(
                member(1L, 1L, "a@x.com", ProjectRole.DEV),
                member(2L, 1L, "b@x.com", ProjectRole.SCRUM_MASTER)
        ));

        mockMvc.perform(get("/api/projects/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].role").value("SCRUM_MASTER"));
    }

    @Test
    @DisplayName("GET /api/projects/{id}/membres (alias) delegates to getMembers")
    void getMembres_alias_delegates() throws Exception {
        when(projectService.getMembers(1L)).thenReturn(List.of(member(1L, 1L, "a@x.com", ProjectRole.DEV)));

        mockMvc.perform(get("/api/projects/1/membres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private Project project(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private ProjectMember member(Long id, Long projectId, String email, ProjectRole role) {
        ProjectMember m = new ProjectMember();
        m.setId(id);
        m.setProjectId(projectId);
        m.setEmail(email);
        m.setRole(role);
        return m;
    }
}