package com.example.Backend.dto;

import lombok.Data;

@Data
public class SprintDTO {
    private Long id;
    private String name;
    private String goal;
    private Double capacityHours;
    private Double remainingCapacityHours;
    private Long projectId;
}
