package com.example.Backend.service;

import com.example.Backend.model.Project;
import com.example.Backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    // Trouver un projet par ID (utile pour la modif et l'affichage détaillé)
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id : " + id));
    }

    // Mettre à jour les informations du projet
    public Project updateProject(Long id, Project details) {
        Project project = findById(id);

        if (details.getName() != null) project.setName(details.getName());
        if (details.getMethodology() != null) project.setMethodology(details.getMethodology());
        if (details.getStartDate() != null) project.setStartDate(details.getStartDate());
        if (details.getEndDate() != null) project.setEndDate(details.getEndDate());
        if (details.getTheoreticalCapacity() != null) project.setTheoreticalCapacity(details.getTheoreticalCapacity());

        return projectRepository.save(project);
    }

    // Supprimer un projet
    public void deleteProject(Long id) {
        Project project = findById(id);
        projectRepository.delete(project);
    }
}