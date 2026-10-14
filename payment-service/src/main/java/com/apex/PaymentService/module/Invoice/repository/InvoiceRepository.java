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

    // ---- org-scoped lookups (HTTP path) ----

    Optional<Invoice> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Optional<Invoice> findByInvoiceNumberAndOrgId(String invoiceNumber, UUID orgId);

    Page<Invoice> findByOrgId(UUID orgId, Pageable pageable);

    Page<Invoice> findByOrgIdAndLeaseId(UUID orgId, UUID leaseId, Pageable pageable);

    Page<Invoice> findByOrgIdAndStatus(UUID orgId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByOrgIdAndLeaseIdAndStatus(UUID orgId, UUID leaseId, InvoiceStatus status, Pageable pageable);

    // ---- internal use only (scheduler, saga) ----

    /** Candidates for the scheduled overdue sweep — spans all orgs. */
    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);

    /** Un-scoped lookup for saga/cross-service flows that lack HTTP context. */
    Optional<Invoice> findById(UUID id);
}