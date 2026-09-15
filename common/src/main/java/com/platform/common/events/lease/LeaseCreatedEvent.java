package com.platform.common.events.lease;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID leaseId;
    private UUID propertyId;
    private UUID tenantId;
    private UUID landlordId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private UUID correlationId;
}
