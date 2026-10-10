package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    private String id;  // MongoDB ObjectId as String

    private Long userId;              // legacy — kept for back-compat, nullable
    private Channel channel;
    private Status status;
    private String eventType;
    private String payload;           // raw event JSON
    private int attempts;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Channel { EMAIL, SMS, IN_APP, WHATSAPP }
    public enum Status { PENDING, SENT, FAILED }
}