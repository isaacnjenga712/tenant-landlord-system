package com.platform.common.dtos.admin;

import java.time.Instant;
import java.util.UUID;

/**
 * Public view of a user for admin dashboards.
 * Never includes password hashes, refresh tokens, or MFA secrets.
 */
public record AdminUserDto(
        UUID id,
        String email,
        String fullName,
        String role,
        boolean active,
        String phone,
        Instant createdAt,
        Instant lastLoginAt
) {}
