package com.platform.common.events.lease;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseCompensationRequiredEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID leaseId;
    private UUID propertyId;
    private UUID tenantId;
    private String reason;
    private UUID correlationId;
}
