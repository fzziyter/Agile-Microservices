package com.example.Backend;

import com.example.Backend.dto.InviteMemberRequest;
import com.example.Backend.model.Project;
import com.example.Backend.model.ProjectMember;
import com.example.Backend.model.ProjectRole;
import com.example.Backend.repository.ProjectMemberRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository       projectRepository;
    @Mock ProjectMemberRepository memberRepository;
    @Mock RestTemplate            restTemplate;

    @InjectMocks ProjectService projectService;

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private Project project(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc");
        p.setCreatorId(1L);
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

    // ─────────────────────────────────────────────────────────
    // createProject
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createProject")
    class CreateProject {

        @Test
        @DisplayName("saves and returns project")
        void savesAndReturns() {
            Project p = project(null, "Agile App");
            when(projectRepository.save(p)).thenReturn(project(1L, "Agile App"));

            Project result = projectService.createProject(p);
            assertThat(result.getId()).isEqualTo(1L);
            verify(projectRepository).save(p);
        }
    }

    // ─────────────────────────────────────────────────────────
    // findAll
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll returns all projects")
    void findAll_returnsList() {
        when(projectRepository.findAll()).thenReturn(List.of(project(1L, "A"), project(2L, "B")));
        assertThat(projectService.findAll()).hasSize(2);
    }

    // ─────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns project when found")
        void found() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P1")));
            assertThat(projectService.findById(1L).getName()).isEqualTo("P1");
        }

        @Test
        @DisplayName("throws RuntimeException when not found")
        void notFound() {
            when(projectRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> projectService.findById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────
    // updateProject
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProject")
    class UpdateProject {

        @Test
        @DisplayName("applies all non-null patch fields")
        void appliesNonNullFields() {
            Project existing = project(1L, "Old");
            when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Project patch = new Project();
            patch.setName("New Name");
            patch.setDescription("New Desc");
            patch.setMethodology("SCRUM");
            patch.setStartDate(LocalDate.of(2025, 1, 1));
            patch.setEndDate(LocalDate.of(2025, 6, 30));
            patch.setTheoreticalCapacity(100);
            patch.setCreatorId(2L);

            Project result = projectService.updateProject(1L, patch);

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getMethodology()).isEqualTo("SCRUM");
            assertThat(result.getTheoreticalCapacity()).isEqualTo(100);
            assertThat(result.getCreatorId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("skips null fields (partial update)")
        void skipsNullFields() {
            Project existing = project(1L, "Keep");
            existing.setMethodology("KANBAN");
            when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Project result = projectService.updateProject(1L, new Project());

            assertThat(result.getName()).isEqualTo("Keep");
            assertThat(result.getMethodology()).isEqualTo("KANBAN");
        }

        @Test
        @DisplayName("throws when project not found")
        void throwsWhenNotFound() {
            when(projectRepository.findById(55L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> projectService.updateProject(55L, new Project()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────
    // deleteProject
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProject")
    class DeleteProject {

        @Test
        @DisplayName("finds then deletes the project")
        void deletesProject() {
            Project p = project(1L, "ToDelete");
            when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

            projectService.deleteProject(1L);

            verify(projectRepository).delete(p);
        }

        @Test
        @DisplayName("throws when project not found")
        void throwsWhenNotFound() {
            when(projectRepository.findById(9L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> projectService.deleteProject(9L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────
    // inviteMember
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("inviteMember")
    class InviteMember {

        @Test
        @DisplayName("saves member with correct fields and returns it")
        void savesMember() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P")));
            when(memberRepository.save(any())).thenAnswer(inv -> {
                ProjectMember m = inv.getArgument(0);
                m.setId(10L);
                return m;
            });

            InviteMemberRequest req = new InviteMemberRequest();
            req.setEmail("alice@example.com");
            req.setRole(ProjectRole.DEV);

            ProjectMember result = projectService.inviteMember(1L, req);

            assertThat(result.getProjectId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("alice@example.com");
            assertThat(result.getRole()).isEqualTo(ProjectRole.DEV);
        }

        @Test
        @DisplayName("sends notification via RestTemplate after saving member")
        void sendsNotification() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P")));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InviteMemberRequest req = new InviteMemberRequest();
            req.setEmail("bob@example.com");
            req.setRole(ProjectRole.QA);

            projectService.inviteMember(1L, req);

            verify(restTemplate).postForObject(
                    eq("http://MS-NOTIFICATIONS/api/notifications"),
                    argThat(n -> n.toString().contains("bob@example.com")),
                    eq(Object.class)
            );
        }

        @Test
        @DisplayName("does not propagate notification failure")
        void doesNotPropagateNotificationFailure() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P")));
            when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(restTemplate.postForObject(anyString(), any(), eq(Object.class)))
                    .thenThrow(new RuntimeException("Notification down"));

            InviteMemberRequest req = new InviteMemberRequest();
            req.setEmail("c@example.com");
            req.setRole(ProjectRole.SCRUM_MASTER);

            assertThatNoException().isThrownBy(() -> projectService.inviteMember(1L, req));
        }

        @Test
        @DisplayName("throws when project not found")
        void throwsWhenProjectNotFound() {
            when(projectRepository.findById(0L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> projectService.inviteMember(0L, new InviteMemberRequest()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────
    // getMembers
    // ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMembers")
    class GetMembers {

        @Test
        @DisplayName("returns members list for existing project")
        void returnsMembersList() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P")));
            when(memberRepository.findByProjectId(1L)).thenReturn(List.of(
                    member(1L, 1L, "a@x.com", ProjectRole.DEV),
                    member(2L, 1L, "b@x.com", ProjectRole.QA)
            ));

            List<ProjectMember> members = projectService.getMembers(1L);
            assertThat(members).hasSize(2);
        }

        @Test
        @DisplayName("throws when project not found")
        void throwsWhenProjectNotFound() {
            when(projectRepository.findById(7L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> projectService.getMembers(7L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}