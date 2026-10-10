package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "recipient_created_idx", def = "{'recipientPublicId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "recipient_unread_idx",  def = "{'recipientPublicId': 1, 'read': 1}")
})
public class Notification {

    @Id
    private String id;

    @Indexed
    private UUID recipientPublicId;

    /** Reserved for future SaaS tenancy. Null today. */
    private UUID organizationId;

    private String eventType;
    private String subject;
    private String body;
    private String rawPayload;

    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
