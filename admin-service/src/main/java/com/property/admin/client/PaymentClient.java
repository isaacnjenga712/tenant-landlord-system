package com.property.admin.client;

import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.property.admin.client.fallback.PaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "PAYMENT-SERVICE",
        path = "/api/v1/internal",
        fallback = PaymentClientFallback.class
)
public interface PaymentClient {

    @GetMapping("/payments/summary")
    PaymentSummaryDto summary();
}