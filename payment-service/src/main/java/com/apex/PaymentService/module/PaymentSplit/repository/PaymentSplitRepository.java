package com.apex.PaymentService.module.PaymentSplit.repository;

import com.apex.PaymentService.module.PaymentSplit.entity.PaymentSplit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentSplitRepository extends JpaRepository<PaymentSplit, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<PaymentSplit> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    List<PaymentSplit> findByOrgIdAndPaymentId(UUID orgId, UUID paymentId);

    Page<PaymentSplit> findByOrgIdAndPaymentId(UUID orgId, UUID paymentId, Pageable pageable);

    Page<PaymentSplit> findByOrgIdAndInvoiceId(UUID orgId, UUID invoiceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.allocatedAmount), 0) FROM PaymentSplit s " +
           "WHERE s.orgId = :orgId AND s.paymentId = :paymentId")
    BigDecimal sumAllocatedAmountByOrgIdAndPaymentId(@Param("orgId") UUID orgId,
                                                     @Param("paymentId") UUID paymentId);
}
