package com.example.Backend.service;

import com.example.Backend.client.ProjectClient;
import com.example.Backend.dto.ProjectDTO;
import com.example.Backend.model.BacklogItem;
import com.example.Backend.repository.BacklogItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BacklogService {

    private final BacklogItemRepository backlogRepository;
    private final ProjectClient projectClient;

    public BacklogService(BacklogItemRepository br, ProjectClient pc) {
        this.backlogRepository = br;
        this.projectClient = pc;
    }

    // Ajouter un item au backlog
    public BacklogItem addItem(Long projectId, BacklogItem item) {
        // On vérifie l'existence du projet via l'appel API Feign
        ProjectDTO project = projectClient.getProjectById(projectId);
        
        if (project == null) {
             throw new RuntimeException("Projet non trouvé dans le microservice MS-PROJECTS");
        }

        item.setProjectId(projectId); 
        return backlogRepository.save(item);
    }

    // Lister le backlog d'un projet spécifique
    public List<BacklogItem> getItemsByProject(Long projectId) {
        return backlogRepository.findByProjectId(projectId);
    }

    // Modifier un item
    public BacklogItem updateItem(Long itemId, BacklogItem details) {
        BacklogItem item = backlogRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item non trouvé"));

        // Utilisation des getters/setters générés par Lombok @Data
        if(details.getTitle() != null) item.setTitle(details.getTitle());
        if(details.getDescription() != null) item.setDescription(details.getDescription());
        if(details.getPriority() != null) item.setPriority(details.getPriority());
        if(details.getType() != null) item.setType(details.getType());

        return backlogRepository.save(item);
    }

    // Supprimer un item
    public void deleteItem(Long itemId) {
        backlogRepository.deleteById(itemId);
    }
}