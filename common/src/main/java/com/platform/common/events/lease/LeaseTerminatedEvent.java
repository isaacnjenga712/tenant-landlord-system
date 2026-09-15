package com.platform.common.events.lease;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseTerminatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID leaseId;
    private UUID propertyId;
    private UUID tenantId;
    private LocalDate terminationDate;
    private String reason;
    private UUID correlationId;
}
