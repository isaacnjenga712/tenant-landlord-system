package com.property.admin.security;

import java.util.UUID;

/**
 * Reads the caller identity from headers set by the gateway's JwtAuthFilter.
 * The gateway already validated the JWT and enforced the ADMIN role,
 * so admin-service trusts these headers.
 */
public final class AdminContext {

    private static final ThreadLocal<AdminIdentity> CURRENT = new ThreadLocal<>();

    private AdminContext() {}

    public static void set(AdminIdentity identity) { CURRENT.set(identity); }
    public static AdminIdentity get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }

    public record AdminIdentity(UUID publicId, String email, String role) {}
}
