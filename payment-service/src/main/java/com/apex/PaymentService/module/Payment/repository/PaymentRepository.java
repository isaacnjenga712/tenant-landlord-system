package com.apex.PaymentService.module.Payment.repository;

import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<Payment> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Optional<Payment> findByGatewayTransactionIdAndOrgId(String gatewayTransactionId, UUID orgId);

    Page<Payment> findByOrgId(UUID orgId, Pageable pageable);

    Page<Payment> findByOrgIdAndTenantId(UUID orgId, UUID tenantId, Pageable pageable);

    Page<Payment> findByOrgIdAndInvoiceId(UUID orgId, UUID invoiceId, Pageable pageable);

    Page<Payment> findByOrgIdAndStatus(UUID orgId, PaymentStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.orgId = :orgId AND p.status = :status")
    BigDecimal sumAmountByOrgIdAndStatus(@Param("orgId") UUID orgId, @Param("status") PaymentStatus status);

    long countByOrgId(UUID orgId);

    long countByOrgIdAndStatus(UUID orgId, PaymentStatus status);

    // ---- internal use only (Kafka / saga / cross-module) ----
    // Bypasses org scoping — MUST NOT be called from user-facing HTTP paths.

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
}