package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Data
public class BacklogItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType type; // USER_STORY, BUG, TASK

    private String priority; // 1 (Haute) à 5 (Basse)

  
    private Long projectId; // Lien vers le projet parent
}

enum ItemType { USER_STORY, BUG, TECHNICAL_TASK }