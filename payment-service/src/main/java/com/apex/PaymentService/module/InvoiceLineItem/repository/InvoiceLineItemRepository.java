package com.apex.PaymentService.module.InvoiceLineItem.repository;

import com.apex.PaymentService.module.InvoiceLineItem.entity.InvoiceLineItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    List<InvoiceLineItem> findByInvoiceId(UUID invoiceId);

    Page<InvoiceLineItem> findByInvoiceId(UUID invoiceId, Pageable pageable);

    void deleteByInvoiceId(UUID invoiceId);
}
