
package com.apex.leaseService.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseRequest {
    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Rent amount is required")
    @DecimalMin(value = "0.01", message = "Rent must be greater than 0")
    private BigDecimal rentAmount;

    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.00", message = "Security deposit cannot be negative")
    private BigDecimal securityDepositAmount;

    @NotEmpty(message = "At least one tenant must be assigned")
    private Set<UUID> tenantIds;      // ✅ now UUID
}