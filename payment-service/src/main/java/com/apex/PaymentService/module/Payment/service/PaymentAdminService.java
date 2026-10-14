package com.apex.PaymentService.module.Payment.service;

import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import com.apex.PaymentService.module.Payment.repository.PaymentRepository;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminPaymentDto;
import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.LongSupplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentSummaryDto summary() {
        UUID orgId = TenantContext.require();

        BigDecimal collected = safeSum(() -> paymentRepository.sumAmountByOrgIdAndStatus(orgId, PaymentStatus.success));
        BigDecimal pending = safeSum(() -> paymentRepository.sumAmountByOrgIdAndStatus(orgId, PaymentStatus.initiated));
        BigDecimal failed = safeSum(() -> paymentRepository.sumAmountByOrgIdAndStatus(orgId, PaymentStatus.failed));

        long total = safeCount(() -> paymentRepository.countByOrgId(orgId));
        long failedCount = safeCount(() -> paymentRepository.countByOrgIdAndStatus(orgId, PaymentStatus.failed));

        PaymentSummaryDto dto = new PaymentSummaryDto(
                collected, pending, failed, total, failedCount);
        log.debug("Payment summary for org {}: {}", orgId, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminPaymentDto> list(int page, int size,
                                              String status, String method) {
        UUID orgId = TenantContext.require();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Payment> result = paymentRepository.findByOrgId(orgId, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.hasNext()
        );
    }

    private AdminPaymentDto toDto(Payment p) {
        return new AdminPaymentDto(
                p.getId() != null ? p.getId().toString() : null,
                p.getInvoiceId() != null ? p.getInvoiceId().toString() : null,
                p.getTenantId() != null ? p.getTenantId().toString() : null,
                p.getAmount(),
                p.getMethod() != null ? p.getMethod().name() : null,
                p.getGatewayTransactionId(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getAppliedToInvoice(),
                p.getProcessedAt() != null
                        ? p.getProcessedAt().toInstant(ZoneOffset.UTC)
                        : null,
                p.getCreatedAt() != null
                        ? p.getCreatedAt().toInstant(ZoneOffset.UTC)
                        : null
        );
    }

    private BigDecimal safeSum(java.util.function.Supplier<BigDecimal> s) {
        try {
            BigDecimal v = s.get();
            return v != null ? v : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Payment sum query failed: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private long safeCount(LongSupplier s) {
        try {
            return s.getAsLong();
        } catch (Exception e) {
            log.warn("Payment count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}