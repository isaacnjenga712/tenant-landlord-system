package com.platform.common.events.mpesa;

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
public class StkPushRequestedEvent {
    private String paymentId;
    private String tenantId;
    private String leaseId;
    private String phone;
    private BigDecimal amount;
    private String accountReference;
    private String transactionDesc;
    private Instant requestedAt;
}
