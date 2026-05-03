package com.example.Backend.controller;

import com.example.Backend.model.Sprint;
import com.example.Backend.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {
    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<Sprint> create(@RequestBody Sprint sprint) {
        return ResponseEntity.status(201).body(sprintService.create(sprint));
    }

    @GetMapping
    public List<Sprint> listAll() {
        return sprintService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.findById(id));
    }

    @GetMapping("/project/{projectId}")
    public List<Sprint> getByProject(@PathVariable Long projectId) {
        return sprintService.findByProject(projectId);
    }

    @GetMapping("/projet/{projectId}")
    public List<Sprint> getByProjet(@PathVariable Long projectId) {
        return getByProject(projectId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        return ResponseEntity.ok(sprintService.update(id, sprint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
