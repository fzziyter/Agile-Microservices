package com.example.Backend.client;

import com.example.Backend.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MS-NOTIFICATIONS")
public interface NotificationClient {
    @PostMapping("/api/notifications")
    void create(@RequestBody NotificationRequest request);
}
