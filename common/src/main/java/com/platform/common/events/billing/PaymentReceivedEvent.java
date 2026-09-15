package com.platform.common.events.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceivedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID paymentId;
    private UUID leaseId;
    private UUID tenantId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paymentDate;
    private String transactionId;      // external reference, keep String
    private UUID correlationId;
}
