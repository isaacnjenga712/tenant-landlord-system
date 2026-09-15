package com.platform.common.events.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID paymentId;
    private UUID leaseId;
    private UUID tenantId;
    private String failureReason;
    private LocalDateTime failedAt;
    private UUID correlationId;
}
