package com.apex.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a user's password is changed (self-service or reset).
 *
 * eventType:     "user.password.changed"
 * eventVersion:  1
 *
 * Consumers:
 *   - Notification service → "Your password was changed" security email
 *   - Session service      → invalidate all active refresh tokens
 *   - Audit service        → compliance log
 *
 * SECURITY: This event deliberately carries NO credentials or hashes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangedEvent {

    private UUID eventId;
    private Long userId;
    private String email;

    /** When the change took effect. */
    private Instant changedAt;

    private String eventType;      
    private int eventVersion;      
}
