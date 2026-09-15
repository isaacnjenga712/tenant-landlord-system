package com.apex.PaymentService.module.Payment.service.external;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvoiceService {
    boolean exists(UUID invoiceId);
    void applyPayment(UUID invoiceId, BigDecimal amount);
    void reversePayment(UUID invoiceId, BigDecimal amount);
}
