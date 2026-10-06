package com.property.admin.service;

import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.property.admin.client.PaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFinanceService {

    private final PaymentClient paymentClient;

    @Cacheable(value = "financeSummary", key = "'summary'")
    public PaymentSummaryDto getSummary() {
        return paymentClient.summary();
    }
}
