package com.example.Backend.dto;

import com.example.Backend.model.TaskStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private TaskStatus status;
    private String comment;
}
