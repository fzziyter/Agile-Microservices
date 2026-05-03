package com.example.Backend.controller;

import com.example.Backend.dto.StatusUpdateRequest;
import com.example.Backend.dto.TaskSprintDTO;
import com.example.Backend.model.BacklogItem;
import com.example.Backend.service.BacklogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backlog")
public class BacklogController {

    private final BacklogService backlogService;

    public BacklogController(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @PostMapping("/{projectId}")
    public ResponseEntity<BacklogItem> create(@PathVariable Long projectId, @RequestBody BacklogItem item) {
        return ResponseEntity.status(201).body(backlogService.addItem(projectId, item));
    }

    @GetMapping("/project/{projectId}")
    public List<BacklogItem> getByProject(@PathVariable Long projectId) {
        return backlogService.getItemsByProject(projectId);
    }

    @GetMapping("/sprint/{sprintId}")
    public List<BacklogItem> getBySprint(@PathVariable Long sprintId) {
        return backlogService.getItemsBySprint(sprintId);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<BacklogItem> update(@PathVariable Long itemId, @RequestBody BacklogItem item) {
        return ResponseEntity.ok(backlogService.updateItem(itemId, item));
    }

    @PatchMapping("/{itemId}/status")
    public ResponseEntity<BacklogItem> updateStatus(@PathVariable Long itemId, @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(backlogService.updateStatus(itemId, request.getStatus(), request.getComment()));
    }

    @PatchMapping("/{itemId}/statut")
    public ResponseEntity<BacklogItem> updateStatut(@PathVariable Long itemId, @RequestBody StatusUpdateRequest request) {
        return updateStatus(itemId, request);
    }

    @GetMapping("/{itemId}/sprint-info")
    public TaskSprintDTO getSprintInfo(@PathVariable Long itemId) {
        return backlogService.getTaskWithSprintInfo(itemId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long itemId) {
        backlogService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
