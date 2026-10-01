package com.property.notification.domain;

/**
 * Canonical event types the notification-engine routes on.
 * The consumer normalizes incoming Kafka eventType strings to these.
 */
public enum NotificationEventType {
    LEASE_CREATED,
    LEASE_APPROVED,
    LEASE_TERMINATED,
    INVOICE_CREATED,
    PAYMENT_RECEIVED_MPESA,
    MAINTENANCE_CREATED,
    MAINTENANCE_RESOLVED,
    USER_REGISTERED,
    USER_LOGIN,
    UNKNOWN;

    /**
     * Normalize a raw eventType string (from any service) to a canonical enum.
     * Handles both dotted ("lease.approved") and flat ("LEASE_APPROVED") forms.
     */
    public static NotificationEventType fromRaw(String raw) {
        if (raw == null || raw.isBlank()) return UNKNOWN;
        String s = raw.toLowerCase().replace('.', '_').replace('-', '_');

        switch (s) {
            // ---- lease ----
            case "lease_lease_created":
            case "lease_created":
                return LEASE_CREATED;
            case "lease_lease_approved":
            case "lease_approved":
                return LEASE_APPROVED;
            case "lease_lease_terminated":
            case "lease_terminated":
                return LEASE_TERMINATED;

            // ---- invoice ----
            case "invoice_invoice_created":
            case "invoice_created":
                return INVOICE_CREATED;

            // ---- mpesa / payment ----
            case "payment_received_mpesa":
            case "mpesa_payment_received":
            case "mpesa_events_stk_result":
                return PAYMENT_RECEIVED_MPESA;

            // ---- maintenance ----
            case "maintenance_ticket_created":
            case "ticket_created":
            case "maintenance_created":
                return MAINTENANCE_CREATED;
            case "maintenance_ticket_resolved":
            case "ticket_resolved":
            case "maintenance_resolved":
                return MAINTENANCE_RESOLVED;

            // ---- auth ----
            case "user_registered":
                return USER_REGISTERED;
            case "user_login":
                return USER_LOGIN;

            default:
                return UNKNOWN;
        }
    }
}