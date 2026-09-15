package com.apex.PaymentService.module.Invoice.repository;

import com.apex.PaymentService.module.Invoice.entity.Invoice;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByLeaseId(UUID leaseId, Pageable pageable);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    Page<Invoice> findByLeaseIdAndStatus(UUID leaseId, InvoiceStatus status, Pageable pageable);
    @Modifying
    @Query("UPDATE Invoice i SET i.status = 'overdue' WHERE i.dueDate <= :date AND i.status IN ('pending', 'partial')")
    int markOverdueInvoices(@Param("date") LocalDate date);
}
