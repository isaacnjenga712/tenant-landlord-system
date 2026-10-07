package com.apex.PaymentService.module.Payment.service;

import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import com.apex.PaymentService.module.Payment.repository.PaymentRepository;
import com.platform.common.dtos.admin.PaymentSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentSummaryDto summary() {
        BigDecimal collected = safeSum(PaymentStatus.success);
        BigDecimal pending = safeSum(PaymentStatus.initiated);
        BigDecimal failed = safeSum(PaymentStatus.failed);

        long total = safeCount(paymentRepository::count);
        long failedCount = safeCount(() -> paymentRepository.countByStatus(PaymentStatus.failed));

        PaymentSummaryDto dto = new PaymentSummaryDto(
                collected, pending, failed, total, failedCount);
        log.debug("Payment summary: {}", dto);
        return dto;
    }

    private BigDecimal safeSum(PaymentStatus status) {
        try {
            BigDecimal v = paymentRepository.sumAmountByStatus(status);
            return v != null ? v : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Payment sum query failed for {}: {}", status, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private long safeCount(java.util.function.LongSupplier s) {
        try { return s.getAsLong(); }
        catch (Exception e) {
            log.warn("Payment count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}
