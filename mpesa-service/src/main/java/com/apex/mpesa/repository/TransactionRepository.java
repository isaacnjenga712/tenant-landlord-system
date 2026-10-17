package com.apex.mpesa.repository;

import com.apex.mpesa.entity.Transaction;
import com.apex.mpesa.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<Transaction> findByIdAndOrgId(String id, String orgId);

    List<Transaction> findByOrgIdAndTenantId(String orgId, String tenantId);

    List<Transaction> findByOrgIdAndLeaseId(String orgId, String leaseId);

    List<Transaction> findByOrgIdAndInvoiceId(String orgId, String invoiceId);

    List<Transaction> findByOrgIdAndStatus(String orgId, TransactionStatus status);

    Optional<Transaction> findByOrgIdAndCheckoutRequestId(String orgId, String checkoutRequestId);

    // ---- internal use only: Safaricom callback has no X-Org-Id header ----
    // checkoutRequestId is globally unique (issued by Safaricom), so this is safe.
    // The resulting transaction's orgId is the source of truth for downstream ops.

    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);

    /** Reconciliation — system job spanning all orgs by design. */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    List<Transaction> findTransactionsBetweenDates(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}