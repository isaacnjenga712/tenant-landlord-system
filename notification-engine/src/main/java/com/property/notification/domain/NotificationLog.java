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

    private Long userId;
    private Channel channel;
    private Status status;
    private String eventType;
    private String payload;    // can be a large string, MongoDB handles it
    private int attempts;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Channel { EMAIL, SMS, IN_APP }
    public enum Status { PENDING, SENT, FAILED }
}
