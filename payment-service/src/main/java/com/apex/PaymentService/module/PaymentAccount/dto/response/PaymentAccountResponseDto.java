package com.apex.PaymentService.module.PaymentAccount.dto.response;

import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentAccountResponseDto {
    private UUID id;
    private EntityType entityType;
    private UUID entityId;
    private BigDecimal trustBalance;
    private BigDecimal operationalBalance;
    private String currency;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
