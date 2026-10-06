package com.property.admin.client.fallback;

import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.property.admin.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentSummaryDto summary() {
        log.warn("payment-service unavailable — returning empty summary");
        return PaymentSummaryDto.empty();
    }
}