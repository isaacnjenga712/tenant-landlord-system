package com.platform.common.events.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiatedEvent {
    private String paymentId;
    private String tenantId;
    private String leaseId;
    private BigDecimal amount;
    private String currency;
    private String method;
    private Instant initiatedAt;
}
