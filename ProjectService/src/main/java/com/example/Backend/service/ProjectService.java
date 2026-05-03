package com.example.Backend.service;

import com.example.Backend.model.Project;
import com.example.Backend.model.ProjectMember;
import com.example.Backend.dto.InviteMemberRequest;
import com.example.Backend.dto.NotificationRequest;
import com.example.Backend.repository.ProjectMemberRepository;
import com.example.Backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RestTemplate restTemplate;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          RestTemplate restTemplate) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.restTemplate = restTemplate;
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
        if (details.getDescription() != null) project.setDescription(details.getDescription());
        if (details.getMethodology() != null) project.setMethodology(details.getMethodology());
        if (details.getStartDate() != null) project.setStartDate(details.getStartDate());
        if (details.getEndDate() != null) project.setEndDate(details.getEndDate());
        if (details.getTheoreticalCapacity() != null) project.setTheoreticalCapacity(details.getTheoreticalCapacity());
        if (details.getCreatorId() != null) project.setCreatorId(details.getCreatorId());

        return projectRepository.save(project);
    }

    // Supprimer un projet
    public void deleteProject(Long id) {
        Project project = findById(id);
        projectRepository.delete(project);
    }

    public ProjectMember inviteMember(Long projectId, InviteMemberRequest request) {
        findById(projectId);
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setEmail(request.getEmail());
        member.setRole(request.getRole());
        ProjectMember saved = projectMemberRepository.save(member);
        notifyInvitation(saved);
        return saved;
    }

    public List<ProjectMember> getMembers(Long projectId) {
        findById(projectId);
        return projectMemberRepository.findByProjectId(projectId);
    }

    private void notifyInvitation(ProjectMember member) {
        try {
            restTemplate.postForObject(
                    "http://MS-NOTIFICATIONS/api/notifications",
                    new NotificationRequest(
                            member.getEmail(),
                            "Invitation au projet avec le role : " + member.getRole(),
                            "INVITATION"
                    ),
                    Object.class
            );
        } catch (Exception ignored) {
            // L'invitation reste valide meme si le service notification est indisponible.
        }
    }
}
