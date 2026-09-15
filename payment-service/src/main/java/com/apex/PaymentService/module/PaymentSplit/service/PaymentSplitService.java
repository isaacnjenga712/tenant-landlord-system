package com.apex.PaymentService.module.PaymentSplit.service;

import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitCreateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitUpdateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.response.PaymentSplitResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentSplitService {

    PaymentSplitResponseDto createSplit(PaymentSplitCreateDto dto);

    PaymentSplitResponseDto getSplit(UUID id);

    List<PaymentSplitResponseDto> getSplitsByPayment(UUID paymentId);

    Page<PaymentSplitResponseDto> getSplitsByPaymentPaged(UUID paymentId, Pageable pageable);

    Page<PaymentSplitResponseDto> getSplitsByInvoicePaged(UUID invoiceId, Pageable pageable);

    PaymentSplitResponseDto updateSplit(UUID id, PaymentSplitUpdateDto dto);

    void deleteSplit(UUID id);

    void deleteAllSplitsByPayment(UUID paymentId);
}
