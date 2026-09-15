package com.apex.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_published_created",
                       columnList = "published, created_at"),
                @Index(name = "idx_outbox_aggregate_id",
                       columnList = "aggregate_id")
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;         // e.g., "User"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;           // e.g., user id

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;             // e.g., "user.registered"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;               // JSON serialized event

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
