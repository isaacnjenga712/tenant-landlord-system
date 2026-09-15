package com.apex.PaymentService.module.SecurityDeposit.dto.response;

import com.apex.PaymentService.module.SecurityDeposit.enums.DepositStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SecurityDepositResponseDto {
    private UUID id;
    private UUID leaseId;
    private BigDecimal totalDeposit;
    private UUID heldInAccountId;
    private BigDecimal currentBalance;
    private BigDecimal interestAccrued;
    private BigDecimal deductionTotal;
    private LocalDate returnedDate;
    private BigDecimal returnAmount;
    private DepositStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
