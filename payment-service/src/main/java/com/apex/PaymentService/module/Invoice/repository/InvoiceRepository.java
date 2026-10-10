package com.apex.PaymentService.module.Invoice.repository;

import com.apex.PaymentService.module.Invoice.entity.Invoice;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByLeaseId(UUID leaseId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByLeaseIdAndStatus(UUID leaseId, InvoiceStatus status, Pageable pageable);

    /** Candidates for overdue sweep — anything not yet finalised. */
    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);
}
