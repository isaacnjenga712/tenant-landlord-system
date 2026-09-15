package com.apex.PaymentService.module.InvoiceLineItem.service.external;

import java.util.UUID;

public interface InvoiceService {
    boolean exists(UUID invoiceId);
}