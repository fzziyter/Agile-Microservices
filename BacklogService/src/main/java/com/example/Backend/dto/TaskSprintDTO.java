package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskSprintDTO {
    private Long taskId;
    private String title;
    private Double estimatedHours;
    private Long sprintId;
    private Double remainingCapacityHours;
}
