package com.apex.leaseService.dtos;

import com.apex.leaseService.entity.LeaseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class LeaseResponse {
    private UUID leaseId;
    private UUID unitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal securityDepositAmount;
    private LeaseStatus status;
    private Set<UUID> tenantIds;      // ✅ now UUID
}