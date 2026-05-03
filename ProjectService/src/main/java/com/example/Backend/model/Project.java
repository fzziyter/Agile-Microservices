package com.example.Backend.model;

import jakarta.persistence.*; // Pour @Entity, @Id, @GeneratedValue
import lombok.Data;           // Pour @Data
import java.time.LocalDate;   // Pour LocalDate

@Entity
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String methodology;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer theoreticalCapacity;
    private Long creatorId;
}
