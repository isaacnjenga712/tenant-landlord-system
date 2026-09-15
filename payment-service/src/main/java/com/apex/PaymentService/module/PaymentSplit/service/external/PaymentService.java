package com.apex.PaymentService.module.PaymentSplit.service.external;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    boolean exists(UUID paymentId);
    BigDecimal getPaymentAmount(UUID paymentId);
}
