// ProjectClient.java
package com.example.Backend.client;

import com.example.Backend.dto.ProjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-PROJECTS") // Nom exact dans Eureka
public interface ProjectClient {

    @GetMapping("/api/projects/{id}")
    ProjectDTO getProjectById(@PathVariable("id") Long id);
}