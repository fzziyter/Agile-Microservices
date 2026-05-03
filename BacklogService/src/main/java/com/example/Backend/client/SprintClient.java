package com.example.Backend.client;

import com.example.Backend.dto.SprintDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-SPRINTS")
public interface SprintClient {
    @GetMapping("/api/sprints/{id}")
    SprintDTO getSprintById(@PathVariable("id") Long id);
}
