package com.apex.PaymentService.module.PaymentSplit.repository;

import com.apex.PaymentService.module.PaymentSplit.entity.PaymentSplit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentSplitRepository extends JpaRepository<PaymentSplit, UUID> {

    List<PaymentSplit> findByPaymentId(UUID paymentId);

    Page<PaymentSplit> findByPaymentId(UUID paymentId, Pageable pageable);

    Page<PaymentSplit> findByInvoiceId(UUID invoiceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.allocatedAmount), 0) FROM PaymentSplit s WHERE s.paymentId = :paymentId")
    BigDecimal sumAllocatedAmountByPaymentId(@Param("paymentId") UUID paymentId);
}
