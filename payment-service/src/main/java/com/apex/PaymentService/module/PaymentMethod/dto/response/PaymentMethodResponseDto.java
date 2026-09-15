package com.apex.PaymentService.module.PaymentMethod.dto.response;

import com.apex.PaymentService.module.PaymentMethod.enums.PaymentMethodType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentMethodResponseDto {
    private UUID id;
    private UUID tenantId;
    private String gatewayCustomerId;
    private String gatewayPaymentMethodId;
    private PaymentMethodType type;
    private String lastFour;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
