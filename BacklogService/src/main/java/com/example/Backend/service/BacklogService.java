package com.example.Backend.service;

import com.example.Backend.client.NotificationClient;
import com.example.Backend.client.ProjectClient;
import com.example.Backend.client.SprintClient;
import com.example.Backend.dto.NotificationRequest;
import com.example.Backend.dto.ProjectDTO;
import com.example.Backend.dto.SprintDTO;
import com.example.Backend.dto.TaskSprintDTO;
import com.example.Backend.model.BacklogItem;
import com.example.Backend.model.TaskStatus;
import com.example.Backend.repository.BacklogItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BacklogService {

    private final BacklogItemRepository backlogRepository;
    private final ProjectClient projectClient;
    private final SprintClient sprintClient;
    private final NotificationClient notificationClient;

    public BacklogService(BacklogItemRepository backlogRepository,
                          ProjectClient projectClient,
                          SprintClient sprintClient,
                          NotificationClient notificationClient) {
        this.backlogRepository = backlogRepository;
        this.projectClient = projectClient;
        this.sprintClient = sprintClient;
        this.notificationClient = notificationClient;
    }

    public BacklogItem addItem(Long projectId, BacklogItem item) {
        ProjectDTO project = projectClient.getProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("Projet non trouve dans MS-PROJECTS");
        }

        item.setProjectId(projectId);
        BacklogItem saved = backlogRepository.save(item);
        notifyAssignment(saved);
        return saved;
    }

    public List<BacklogItem> getItemsByProject(Long projectId) {
        return backlogRepository.findByProjectId(projectId);
    }

    public List<BacklogItem> getItemsBySprint(Long sprintId) {
        return backlogRepository.findBySprintId(sprintId);
    }

    public BacklogItem updateItem(Long itemId, BacklogItem details) {
        BacklogItem item = backlogRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item non trouve"));

        if (details.getTitle() != null) item.setTitle(details.getTitle());
        if (details.getDescription() != null) item.setDescription(details.getDescription());
        if (details.getPriority() != null) item.setPriority(details.getPriority());
        if (details.getType() != null) item.setType(details.getType());
        if (details.getStatus() != null) item.setStatus(details.getStatus());
        if (details.getEstimatedHours() != null) item.setEstimatedHours(details.getEstimatedHours());
        if (details.getStoryPoints() != null) item.setStoryPoints(details.getStoryPoints());
        if (details.getAssignedToId() != null) item.setAssignedToId(details.getAssignedToId());
        if (details.getSprintId() != null) item.setSprintId(details.getSprintId());
        if (details.getBlockedComment() != null) item.setBlockedComment(details.getBlockedComment());
        if (details.getQaValidated() != null) item.setQaValidated(details.getQaValidated());
        if (details.getQaComment() != null) item.setQaComment(details.getQaComment());
        if (details.getPoValidated() != null) item.setPoValidated(details.getPoValidated());

        BacklogItem saved = backlogRepository.save(item);
        notifyAssignment(saved);
        return saved;
    }

    public BacklogItem updateStatus(Long itemId, TaskStatus status, String comment) {
        BacklogItem item = backlogRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item non trouve"));
        item.setStatus(status);
        if (status == TaskStatus.BLOCKED) {
            item.setBlockedComment(comment);
        }
        if (status == TaskStatus.IN_PROGRESS || status == TaskStatus.DONE) {
            item.setBlockedComment(null);
        }
        BacklogItem saved = backlogRepository.save(item);
        notifyStatusChange(saved);
        return saved;
    }

    public TaskSprintDTO getTaskWithSprintInfo(Long itemId) {
        BacklogItem item = backlogRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item non trouve"));
        if (item.getSprintId() == null) {
            return new TaskSprintDTO(item.getId(), item.getTitle(), item.getEstimatedHours(), null, null);
        }
        SprintDTO sprint = sprintClient.getSprintById(item.getSprintId());
        return new TaskSprintDTO(
                item.getId(),
                item.getTitle(),
                item.getEstimatedHours(),
                item.getSprintId(),
                sprint.getRemainingCapacityHours()
        );
    }

    public void deleteItem(Long itemId) {
        backlogRepository.deleteById(itemId);
    }

    private void notifyAssignment(BacklogItem item) {
        if (item.getAssignedToId() == null) return;
        try {
            notificationClient.create(new NotificationRequest(
                    item.getAssignedToId(),
                    "Tache assignee : " + item.getTitle(),
                    "ASSIGNMENT"
            ));
        } catch (Exception ex) {
            System.err.println("Notification assignment failed for task " + item.getId() + ": " + ex.getMessage());
        }
    }

    private void notifyStatusChange(BacklogItem item) {
        if (item.getAssignedToId() == null) return;
        try {
            notificationClient.create(new NotificationRequest(
                    item.getAssignedToId(),
                    "Statut change pour " + item.getTitle() + " : " + item.getStatus(),
                    "STATUS_CHANGE"
            ));
        } catch (Exception ex) {
            System.err.println("Notification status change failed for task " + item.getId() + ": " + ex.getMessage());
        }
    }
}
