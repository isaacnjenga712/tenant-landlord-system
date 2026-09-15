package com.property.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {
    private Long userId;
    private String propertyId;
    private BigDecimal amount;
    private String paymentMethod;
}
