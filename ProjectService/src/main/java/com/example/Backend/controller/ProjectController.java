package com.example.Backend.controller;

import com.example.Backend.dto.InviteMemberRequest;
import com.example.Backend.model.Project;
import com.example.Backend.model.ProjectMember;
import com.example.Backend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService ps) {
        this.projectService = ps;
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        return ResponseEntity.status(201).body(projectService.createProject(project));
    }

    @GetMapping
    public List<Project> listAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(id, project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMember> inviteMember(@PathVariable Long id, @RequestBody InviteMemberRequest request) {
        return ResponseEntity.status(201).body(projectService.inviteMember(id, request));
    }

    @PostMapping("/{id}/membres")
    public ResponseEntity<ProjectMember> inviteMembre(@PathVariable Long id, @RequestBody InviteMemberRequest request) {
        return inviteMember(id, request);
    }

    @GetMapping("/{id}/members")
    public List<ProjectMember> getMembers(@PathVariable Long id) {
        return projectService.getMembers(id);
    }

    @GetMapping("/{id}/membres")
    public List<ProjectMember> getMembres(@PathVariable Long id) {
        return getMembers(id);
    }
}
