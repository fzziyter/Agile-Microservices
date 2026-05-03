package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BacklogItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    private String priority;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    private Double estimatedHours;
    private Integer storyPoints;
    private Long assignedToId;
    private Long sprintId;
    private Long projectId;
    private String blockedComment;
    private Boolean qaValidated = false;
    private String qaComment;
    private Boolean poValidated = false;
}
