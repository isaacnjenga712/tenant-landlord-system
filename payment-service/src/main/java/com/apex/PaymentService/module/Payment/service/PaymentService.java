package com.apex.PaymentService.module.Payment.service;

import com.apex.PaymentService.module.Payment.dto.request.PaymentCreateDto;
import com.apex.PaymentService.module.Payment.dto.request.PaymentUpdateDto;
import com.apex.PaymentService.module.Payment.dto.response.PaymentResponseDto;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    // ---------- CRUD ----------
    PaymentResponseDto createPayment(PaymentCreateDto dto);
    PaymentResponseDto getPayment(UUID id);
    Page<PaymentResponseDto> listPayments(UUID tenantId, UUID invoiceId, PaymentStatus status, Pageable pageable);
    PaymentResponseDto updatePayment(UUID id, PaymentUpdateDto dto);
    void deletePayment(UUID id);

    // ---------- Processing ----------
    PaymentResponseDto processPayment(UUID id, PaymentStatus newStatus, String gatewayResponse);
    PaymentResponseDto refundPayment(UUID id);
    void applyPaymentToInvoice(UUID paymentId);

    // ---------- Validation (used by other modules) ----------
    boolean exists(UUID paymentId);
    BigDecimal getPaymentAmount(UUID paymentId);   // ✅ NEW – used by PaymentSplit
}