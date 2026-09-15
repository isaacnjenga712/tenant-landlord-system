package com.apex.auth.event;

import com.apex.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a new user completes registration.
 *
 * eventType:     "user.registered"
 * eventVersion:  1
 *
 * Consumers:
 *   - Notification service → send welcome email
 *   - Analytics service    → increment signup counter
 *   - Onboarding service   → seed tenant/landlord dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    /** Unique event identifier — use for idempotent consumption. */
    private UUID eventId;

    /** Persistent user id assigned by the database. */
    private Long userId;

    /** Login identifier. Also used as the Kafka message key. */
    private String email;

    /** Display name. */
    private String fullName;

    /** TENANT, LANDLORD, or ADMIN. */
    private Role role;

    /** When the user record was created. */
    private Instant registeredAt;

    /** Logical type — "user.registered". */
    private String eventType;

    /** Schema version — 1 for initial release. */
    private int eventVersion;
}
