package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    private String id;

    /** SaaS tenant (org) that owns this log entry. */
    @Indexed
    private UUID organizationId;

    private UUID recipientPublicId;   // new — was Long userId (legacy, unused)
    private Channel channel;
    private Status status;
    private String eventType;
    private String payload;
    private int attempts;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Channel { EMAIL, SMS, IN_APP, WHATSAPP }
    public enum Status { PENDING, SENT, FAILED }
}