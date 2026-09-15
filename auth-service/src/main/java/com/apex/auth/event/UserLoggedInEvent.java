package com.apex.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a user successfully authenticates.
 *
 * eventType:     "user.login"
 * eventVersion:  1
 *
 * Consumers:
 *   - Audit service       → immutable login trail
 *   - Analytics service   → DAU/MAU metrics
 *   - Security service    → anomaly detection (new IP/device)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {

    private UUID eventId;
    private Long userId;
    private String email;

    /** When authentication succeeded. */
    private Instant loginAt;

    private String eventType;      // "user.login"
    private int eventVersion;      // 1

    // Optional fields you may add later (bump eventVersion to 2):
    // private String ipAddress;
    // private String userAgent;
    // private String deviceFingerprint;
}
