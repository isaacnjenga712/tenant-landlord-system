package com.apex.PaymentService.module.DepositDeduction.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DepositDeductionResponseDto {
    private UUID id;
    private UUID depositId;
    private String description;
    private BigDecimal amount;
    private String supportingDocumentUrl;
    private UUID approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}