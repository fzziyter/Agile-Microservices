package com.example.Backend.service;

import com.example.Backend.model.Sprint;
import com.example.Backend.repository.SprintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SprintService {
    private final SprintRepository sprintRepository;

    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    public Sprint create(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public Sprint findById(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint non trouve avec l'id : " + id));
    }

    public List<Sprint> findByProject(Long projectId) {
        return sprintRepository.findByProjectId(projectId);
    }

    public Sprint update(Long id, Sprint details) {
        Sprint sprint = findById(id);
        if (details.getName() != null) sprint.setName(details.getName());
        if (details.getGoal() != null) sprint.setGoal(details.getGoal());
        if (details.getStartDate() != null) sprint.setStartDate(details.getStartDate());
        if (details.getEndDate() != null) sprint.setEndDate(details.getEndDate());
        if (details.getCapacityHours() != null) sprint.setCapacityHours(details.getCapacityHours());
        if (details.getRemainingCapacityHours() != null) sprint.setRemainingCapacityHours(details.getRemainingCapacityHours());
        if (details.getStatus() != null) sprint.setStatus(details.getStatus());
        if (details.getProjectId() != null) sprint.setProjectId(details.getProjectId());
        return sprintRepository.save(sprint);
    }

    public void delete(Long id) {
        sprintRepository.delete(findById(id));
    }
}
