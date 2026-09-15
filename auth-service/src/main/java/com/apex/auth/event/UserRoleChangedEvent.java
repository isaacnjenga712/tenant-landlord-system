package com.apex.auth.event;

import com.apex.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an admin changes a user's role.
 *
 * eventType:     "user.role.changed"
 * eventVersion:  1
 *
 * Consumers:
 *   - Authorization cache → invalidate cached permissions
 *   - Notification service → alert the affected user
 *   - Audit service        → compliance log with before/after
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangedEvent {

    private UUID eventId;
    private Long userId;
    private String email;

    /** Role before the change. */
    private Role oldRole;

    /** Role after the change. */
    private Role newRole;

    /** When the change took effect. */
    private Instant changedAt;

    private String eventType;      // "user.role.changed"
    private int eventVersion;      // 1
}
