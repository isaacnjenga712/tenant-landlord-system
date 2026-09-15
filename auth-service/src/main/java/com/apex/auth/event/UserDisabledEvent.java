package com.apex.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an admin disables a user account.
 *
 * eventType:     "user.disabled"
 * eventVersion:  1
 *
 * Consumers:
 *   - Session service      → revoke all active tokens immediately
 *   - Notification service → notify the user (if allowed by policy)
 *   - Audit service        → compliance log with reason
 *   - Billing service      → stop metering / suspend subscription
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDisabledEvent {

    private UUID eventId;
    private Long userId;
    private String email;

    /** Free-text reason — "Disabled by admin", "Payment failed", "Policy violation". */
    private String reason;

    /** When the account was disabled. */
    private Instant disabledAt;

    private String eventType;      // "user.disabled"
    private int eventVersion;      // 1
}
