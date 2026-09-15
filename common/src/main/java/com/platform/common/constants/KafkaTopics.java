package com.platform.common.constants;

public final class KafkaTopics {

    private KafkaTopics() {}

    // === LEASE ===
    public static final String LEASE_CREATED = "lease.lease.created";
    public static final String LEASE_TERMINATED = "lease.lease.terminated";
    public static final String LEASE_COMPENSATION_REQUIRED = "lease.compensation.required";

    // === PROPERTY ===
    public static final String PROPERTY_REGISTERED = "property.property.registered";
    public static final String PROPERTY_UPDATED = "property.property.updated";

    // === BILLING ===
    public static final String PAYMENT_RECEIVED = "billing.payment.received";
    public static final String PAYMENT_FAILED = "billing.payment.failed";
    public static final String INVOICE_GENERATED = "billing.invoice.generated";
    public static final String INVOICE_VOIDED = "billing.invoice.voided";

    // === MAINTENANCE ===
    public static final String TICKET_CREATED = "maintenance.ticket.created";
    public static final String TICKET_RESOLVED = "maintenance.ticket.resolved";

    // === DEAD LETTER TOPICS ===
    public static final String DLT_SUFFIX = "-dlt";
    public static final String LEASE_CREATED_DLT = LEASE_CREATED + DLT_SUFFIX;
    public static final String PAYMENT_RECEIVED_DLT = PAYMENT_RECEIVED + DLT_SUFFIX;
    public static final String PAYMENT_FAILED_DLT = PAYMENT_FAILED + DLT_SUFFIX;
}
