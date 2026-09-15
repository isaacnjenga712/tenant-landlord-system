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

    /**
     * Finds a transaction by its Safaricom CheckoutRequestID (unique).
     */
    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);

    /**
     * Lists all transactions for a given tenant (UUID string).
     */
    List<Transaction> findByTenantId(String tenantId);

    /**
     * Lists all transactions for a given lease (UUID string).
     */
    List<Transaction> findByLeaseId(String leaseId);

    /**
     * Lists all transactions for a given invoice (UUID string).
     */
    List<Transaction> findByInvoiceId(String invoiceId);

    /**
     * Finds transactions for a tenant with a specific status.
     */
    List<Transaction> findByTenantIdAndStatus(String tenantId, TransactionStatus status);

    /**
     * Finds transactions created within a date range (for reconciliation).
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    List<Transaction> findTransactionsBetweenDates(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}
