package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String email;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;
}
