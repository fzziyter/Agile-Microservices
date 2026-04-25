package com.example.Backend.repository;

import com.example.Backend.model.BacklogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BacklogItemRepository extends JpaRepository<BacklogItem, Long> {
    // Permet de récupérer uniquement les items liés à un projet précis
    List<BacklogItem> findByProjectId(Long projectId);
}