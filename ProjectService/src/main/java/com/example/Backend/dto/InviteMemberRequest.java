package com.example.Backend.dto;

import com.example.Backend.model.ProjectRole;
import lombok.Data;

@Data
public class InviteMemberRequest {
    private String email;
    private ProjectRole role;
}
