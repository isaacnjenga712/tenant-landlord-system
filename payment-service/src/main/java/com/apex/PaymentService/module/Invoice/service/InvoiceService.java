package com.apex.PaymentService.module.Invoice.service;

import com.apex.PaymentService.module.Invoice.dto.request.InvoiceCreateDto;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceUpdateDto;
import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

public interface InvoiceService {
    InvoiceResponseDto createInvoice(InvoiceCreateDto dto);
    InvoiceResponseDto getInvoice(UUID id);
    InvoiceResponseDto getInvoiceByNumber(String invoiceNumber);
    Page<InvoiceResponseDto> listInvoices(UUID leaseId, InvoiceStatus status, Pageable pageable);
    InvoiceResponseDto updateInvoice(UUID id, InvoiceUpdateDto dto);
    void voidInvoice(UUID id);
    boolean exists(UUID invoiceId);
    void applyPayment(UUID invoiceId, BigDecimal amount);
    void reversePayment(UUID invoiceId, BigDecimal amount);
    InvoiceResponseDto applyPaymentAndGetInvoice(UUID id, BigDecimal amount);
    void applyLateFee(UUID id, BigDecimal feeAmount);
    void markOverdueInvoices();
}
