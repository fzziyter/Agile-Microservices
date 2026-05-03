package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double capacityHours;
    private Double remainingCapacityHours;

    @Enumerated(EnumType.STRING)
    private SprintStatus status = SprintStatus.PLANNED;

    private Long projectId;

    @PrePersist
    public void prePersist() {
        if (remainingCapacityHours == null) {
            remainingCapacityHours = capacityHours;
        }
    }
}
