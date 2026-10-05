package com.property.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private UUID recipientPublicId;
    private String eventType;
    private String subject;
    private String body;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
