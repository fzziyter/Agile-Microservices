package com.example.Backend;

import com.example.Backend.model.Sprint;
import com.example.Backend.model.SprintStatus;
import com.example.Backend.repository.SprintRepository;
import com.example.Backend.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SprintService – Unit Tests")
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @InjectMocks
    private SprintService sprintService;

    private Sprint sprint;

    @BeforeEach
    void setUp() {
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

    // ─── create ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() – persists and returns the sprint")
    void create_shouldSaveAndReturn() {
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);

        Sprint result = sprintService.create(sprint);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sprint 1");
        verify(sprintRepository, times(1)).save(sprint);
    }

    @Test
    @DisplayName("create() – default status is PLANNED")
    void create_shouldDefaultStatusToPlanned() {
        when(sprintRepository.save(any())).thenReturn(sprint);

        Sprint result = sprintService.create(sprint);

        assertThat(result.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    // ─── findAll ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() – returns all sprints")
    void findAll_shouldReturnAll() {
        Sprint s2 = new Sprint();
        s2.setId(2L);
        s2.setName("Sprint 2");
        when(sprintRepository.findAll()).thenReturn(List.of(sprint, s2));

        List<Sprint> result = sprintService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Sprint::getName).containsExactly("Sprint 1", "Sprint 2");
    }

    @Test
    @DisplayName("findAll() – returns empty list when no sprints exist")
    void findAll_shouldReturnEmpty() {
        when(sprintRepository.findAll()).thenReturn(List.of());

        assertThat(sprintService.findAll()).isEmpty();
    }

    // ─── findById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() – returns sprint when found")
    void findById_shouldReturnSprint() {
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Sprint result = sprintService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sprint 1");
    }

    @Test
    @DisplayName("findById() – throws RuntimeException when not found")
    void findById_shouldThrowWhenNotFound() {
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ─── findByProject ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByProject() – returns sprints for given projectId")
    void findByProject_shouldReturnSprintsForProject() {
        when(sprintRepository.findByProjectId(100L)).thenReturn(List.of(sprint));

        List<Sprint> result = sprintService.findByProject(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("findByProject() – returns empty list for unknown projectId")
    void findByProject_shouldReturnEmptyForUnknownProject() {
        when(sprintRepository.findByProjectId(999L)).thenReturn(List.of());

        assertThat(sprintService.findByProject(999L)).isEmpty();
    }

    // ─── update ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() – updates all non-null fields and saves")
    void update_shouldUpdateAllNonNullFields() {
        Sprint details = new Sprint();
        details.setName("Sprint 1 Updated");
        details.setGoal("New goal");
        details.setStartDate(LocalDate.of(2025, 2, 1));
        details.setEndDate(LocalDate.of(2025, 2, 14));
        details.setCapacityHours(100.0);
        details.setRemainingCapacityHours(90.0);
        details.setStatus(SprintStatus.IN_PROGRESS);
        details.setProjectId(200L);

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sprint result = sprintService.update(1L, details);

        assertThat(result.getName()).isEqualTo("Sprint 1 Updated");
        assertThat(result.getGoal()).isEqualTo("New goal");
        assertThat(result.getStatus()).isEqualTo(SprintStatus.IN_PROGRESS);
        assertThat(result.getCapacityHours()).isEqualTo(100.0);
        assertThat(result.getProjectId()).isEqualTo(200L);
        verify(sprintRepository).save(sprint);
    }

    @Test
    @DisplayName("update() – skips null fields (partial update)")
    void update_shouldSkipNullFields() {
        Sprint partialDetails = new Sprint();
        partialDetails.setName("Only Name Changed");
        // all other fields null → should not overwrite existing values

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sprint result = sprintService.update(1L, partialDetails);

        assertThat(result.getName()).isEqualTo("Only Name Changed");
        assertThat(result.getGoal()).isEqualTo("Deliver authentication module");
        assertThat(result.getCapacityHours()).isEqualTo(80.0);
        assertThat(result.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    @DisplayName("update() – throws RuntimeException when sprint not found")
    void update_shouldThrowWhenNotFound() {
        when(sprintRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.update(999L, new Sprint()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // ─── delete ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() – finds sprint then calls repository delete")
    void delete_shouldFindAndDelete() {
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        doNothing().when(sprintRepository).delete(sprint);

        sprintService.delete(1L);

        verify(sprintRepository, times(1)).findById(1L);
        verify(sprintRepository, times(1)).delete(sprint);
    }

    @Test
    @DisplayName("delete() – throws RuntimeException when sprint not found")
    void delete_shouldThrowWhenNotFound() {
        when(sprintRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(sprintRepository, never()).delete(any());
    }
}