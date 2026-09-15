package com.property.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InAppNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(Long userId, String message) {
        // Send to a user-specific queue, e.g., /queue/notifications
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                message
        );
    }
}
