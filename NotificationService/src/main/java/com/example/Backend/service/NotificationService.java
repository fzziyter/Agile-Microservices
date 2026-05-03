package com.example.Backend.service;

import com.example.Backend.model.Notification;
import com.example.Backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findByUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findByEmail(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvee"));
        notification.setReadFlag(true);
        return notificationRepository.save(notification);
    }

    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }
}
