package com.apex.PaymentService.module.InvoiceLineItem.repository;

import com.apex.PaymentService.module.InvoiceLineItem.entity.InvoiceLineItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<InvoiceLineItem> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    List<InvoiceLineItem> findByOrgIdAndInvoiceId(UUID orgId, UUID invoiceId);

    Page<InvoiceLineItem> findByOrgIdAndInvoiceId(UUID orgId, UUID invoiceId, Pageable pageable);

    void deleteByOrgIdAndInvoiceId(UUID orgId, UUID invoiceId);
}
