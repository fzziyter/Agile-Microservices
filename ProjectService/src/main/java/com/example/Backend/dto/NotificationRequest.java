package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationRequest {
    private String recipientEmail;
    private String message;
    private String type;
}
