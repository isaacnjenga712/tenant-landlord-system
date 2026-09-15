package com.apex.PaymentService.module.PaymentMethod.service;

import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodCreateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodUpdateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.response.PaymentMethodResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentMethodService {

    PaymentMethodResponseDto createPaymentMethod(PaymentMethodCreateDto dto);

    PaymentMethodResponseDto getPaymentMethod(UUID id);

    Page<PaymentMethodResponseDto> getPaymentMethodsByTenant(UUID tenantId, Pageable pageable);

    PaymentMethodResponseDto updatePaymentMethod(UUID id, PaymentMethodUpdateDto dto);

    void deletePaymentMethod(UUID id);

    PaymentMethodResponseDto setDefaultMethod(UUID id);

    PaymentMethodResponseDto toggleActive(UUID id, boolean active);
}
