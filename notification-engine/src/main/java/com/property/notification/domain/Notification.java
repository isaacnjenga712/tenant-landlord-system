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
    @CompoundIndex(
            name = "org_recipient_created_idx",
            def = "{'organizationId': 1, 'recipientPublicId': 1, 'createdAt': -1}"),
    @CompoundIndex(
            name = "org_recipient_unread_idx",
            def = "{'organizationId': 1, 'recipientPublicId': 1, 'read': 1}")
})
public class Notification {

    @Id
    private String id;

    /** SaaS tenant (org) that owns this notification. */
    @Indexed
    private UUID organizationId;

    @Indexed
    private UUID recipientPublicId;

    private String eventType;
    private String subject;
    private String body;
    private String rawPayload;

    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}