package com.property.notification.domain;

/**
 * Canonical event types the notification-engine routes on.
 * The consumer normalizes incoming Kafka eventType strings to these.
 */
public enum NotificationEventType {
    LEASE_APPROVED,
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
        // strip common prefixes
        s = s.replace("maintenance_", "maintenance_");
        switch (s) {
            case "lease_approved":
            case "lease_lease_approved":
            case "lease.approved":
                return LEASE_APPROVED;
            case "invoice_created":
            case "invoice_invoice_created":
                return INVOICE_CREATED;
            case "payment_received_mpesa":
            case "mpesa_payment_received":
            case "mpesa_events_stk_result":
                return PAYMENT_RECEIVED_MPESA;
            case "maintenance_ticket_created":
            case "ticket_created":
            case "maintenance_created":
                return MAINTENANCE_CREATED;
            case "maintenance_ticket_resolved":
            case "ticket_resolved":
            case "maintenance_resolved":
                return MAINTENANCE_RESOLVED;
            case "user_registered":
                return USER_REGISTERED;
            case "user_login":
                return USER_LOGIN;
            default:
                return UNKNOWN;
        }
    }
}
