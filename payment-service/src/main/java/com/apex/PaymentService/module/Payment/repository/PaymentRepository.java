package com.apex.PaymentService.module.Payment.repository;

import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    Page<Payment> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
