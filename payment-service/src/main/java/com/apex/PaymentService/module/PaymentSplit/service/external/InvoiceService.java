package com.apex.PaymentService.module.PaymentSplit.service.external;

import java.util.UUID;

public interface InvoiceService {
    boolean exists(UUID invoiceId);
}
