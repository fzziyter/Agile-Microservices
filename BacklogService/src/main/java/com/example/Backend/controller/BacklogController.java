package com.example.Backend.controller;

import com.example.Backend.model.BacklogItem;
import com.example.Backend.service.BacklogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backlog")
public class BacklogController {

    private final BacklogService backlogService;

    public BacklogController(BacklogService bs) {
        this.backlogService = bs;
    }

    @PostMapping("/{projectId}")
    public ResponseEntity<BacklogItem> create(@PathVariable Long projectId, @RequestBody BacklogItem item) {
        return ResponseEntity.status(201).body(backlogService.addItem(projectId, item));
    }

    @GetMapping("/project/{projectId}")
    public List<BacklogItem> getByProject(@PathVariable Long projectId) {
        return backlogService.getItemsByProject(projectId);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<BacklogItem> update(@PathVariable Long itemId, @RequestBody BacklogItem item) {
        return ResponseEntity.ok(backlogService.updateItem(itemId, item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long itemId) {
        backlogService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}