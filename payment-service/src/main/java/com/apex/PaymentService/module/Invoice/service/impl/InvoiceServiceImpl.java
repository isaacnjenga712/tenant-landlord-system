package com.apex.PaymentService.module.Invoice.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceCreateDto;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceUpdateDto;
import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
import com.apex.PaymentService.module.Invoice.entity.Invoice;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import com.apex.PaymentService.module.Invoice.mapper.InvoiceMapper;
import com.apex.PaymentService.module.Invoice.producer.InvoiceEventProducer;
import com.apex.PaymentService.module.Invoice.repository.InvoiceRepository;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import com.apex.PaymentService.module.Invoice.service.external.LeaseValidationService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final LeaseValidationService leaseValidationService;
    private final InvoiceEventProducer invoiceEventProducer;

    // ---------- CRUD ----------

    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceCreateDto dto) {
        UUID orgId = TenantContext.require();

        if (!leaseValidationService.exists(dto.getLeaseId())) {
            throw new ResourceNotFoundException("Lease not found with id: " + dto.getLeaseId());
        }
        if (repository.findByInvoiceNumberAndOrgId(dto.getInvoiceNumber(), orgId).isPresent()) {
            throw new BusinessException("Invoice number already exists: " + dto.getInvoiceNumber());
        }

        Invoice invoice = mapper.toEntity(dto);
        invoice.setOrgId(orgId);
        invoice.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO);
        invoice.updateStatus();
        Invoice saved = repository.save(invoice);
        log.info("Created invoice: {} for lease: {} org: {}", saved.getInvoiceNumber(), saved.getLeaseId(), orgId);

        InvoiceResponseDto response = mapper.toResponseDto(saved);

        try {
            LeaseValidationService.LeaseInfo info = leaseValidationService.getLease(dto.getLeaseId());
            invoiceEventProducer.publishInvoiceCreated(response, info.tenantId(), info.landlordId());
        } catch (Exception e) {
            log.warn("Failed to publish InvoiceCreatedEvent for {}: {}",
                    response.getInvoiceNumber(), e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoice(UUID id) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return mapper.toResponseDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoiceByNumber(String invoiceNumber) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByInvoiceNumberAndOrgId(invoiceNumber, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
        return mapper.toResponseDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> listInvoices(UUID leaseId, InvoiceStatus status, Pageable pageable) {
        UUID orgId = TenantContext.require();

        if (leaseId != null && status != null) {
            return repository.findByOrgIdAndLeaseIdAndStatus(orgId, leaseId, status, pageable)
                    .map(mapper::toResponseDto);
        } else if (leaseId != null) {
            return repository.findByOrgIdAndLeaseId(orgId, leaseId, pageable)
                    .map(mapper::toResponseDto);
        } else if (status != null) {
            return repository.findByOrgIdAndStatus(orgId, status, pageable)
                    .map(mapper::toResponseDto);
        } else {
            return repository.findByOrgId(orgId, pageable).map(mapper::toResponseDto);
        }
    }

    @Override
    @Transactional
    public InvoiceResponseDto updateInvoice(UUID id, InvoiceUpdateDto dto) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.voided || invoice.getStatus() == InvoiceStatus.paid) {
            throw new BusinessException("Cannot update " + invoice.getStatus() + " invoice");
        }

        if (dto.getDueDate() != null) invoice.setDueDate(dto.getDueDate());
        if (dto.getTotalAmount() != null) {
            if (dto.getTotalAmount().compareTo(invoice.getPaidAmount()) < 0) {
                throw new BusinessException("Total amount cannot be less than paid amount");
            }
            invoice.setTotalAmount(dto.getTotalAmount());
        }
        if (dto.getPaidAmount() != null) {
            if (dto.getPaidAmount().compareTo(invoice.getTotalAmount()) > 0) {
                throw new BusinessException("Paid amount cannot exceed total amount");
            }
            invoice.setPaidAmount(dto.getPaidAmount());
        }
        if (dto.getGracePeriodDays() != null) invoice.setGracePeriodDays(dto.getGracePeriodDays());
        if (dto.getMetadata() != null) invoice.setMetadata(dto.getMetadata());
        if (dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("void")) {
            invoice.setStatus(InvoiceStatus.voided);
        }

        invoice.updateStatus();
        Invoice updated = repository.save(invoice);
        log.info("Updated invoice: {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void voidInvoice(UUID id) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.paid) {
            throw new BusinessException("Cannot void a paid invoice");
        }
        invoice.setStatus(InvoiceStatus.voided);
        repository.save(invoice);
        log.info("Voided invoice: {}", id);
    }

    // ---------- Payment Integration ----------

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID invoiceId) {
        UUID orgId = TenantContext.get();
        // If invoked from an HTTP flow, enforce org. Otherwise (saga/cross-module) allow un-scoped.
        if (orgId != null) {
            return repository.existsByIdAndOrgId(invoiceId, orgId);
        }
        return repository.existsById(invoiceId);
    }

    @Override
    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void applyPayment(UUID invoiceId, BigDecimal amount) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(invoiceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.applyPayment(amount);
        repository.save(invoice);
        log.info("Applied payment of {} to invoice {}", amount, invoiceId);
    }

    @Override
    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void reversePayment(UUID invoiceId, BigDecimal amount) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(invoiceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.reversePayment(amount);
        repository.save(invoice);
        log.info("Reversed payment of {} on invoice {}", amount, invoiceId);
    }

    @Override
    @Transactional
    public InvoiceResponseDto applyPaymentAndGetInvoice(UUID id, BigDecimal amount) {
        applyPayment(id, amount);
        return getInvoice(id);
    }

    // ---------- Late Fees & Overdue ----------

    @Override
    @Transactional
    public void applyLateFee(UUID id, BigDecimal feeAmount) {
        UUID orgId = TenantContext.require();
        Invoice invoice = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.paid || invoice.getStatus() == InvoiceStatus.voided) {
            throw new BusinessException("Cannot add late fee to " + invoice.getStatus() + " invoice");
        }
        if (invoice.getLateFeeApplied()) {
            throw new BusinessException("Late fee already applied to this invoice");
        }
        invoice.setTotalAmount(invoice.getTotalAmount().add(feeAmount));
        invoice.setLateFeeApplied(true);
        invoice.updateStatus();
        repository.save(invoice);
        log.info("Applied late fee of {} to invoice {}", feeAmount, id);
    }

    @Override
    @Transactional
    public void markOverdueInvoices() {
        // Scheduled system job — no TenantContext, spans all orgs by design.
        LocalDate today = LocalDate.now();
        List<Invoice> candidates = repository.findByStatusIn(
                List.of(InvoiceStatus.pending, InvoiceStatus.partial));

        int updated = 0;
        for (Invoice inv : candidates) {
            if (inv.getDueDate() == null) continue;

            int grace = inv.getGracePeriodDays() != null ? inv.getGracePeriodDays() : 0;
            LocalDate overdueThreshold = inv.getDueDate().plusDays(grace);

            if (today.isAfter(overdueThreshold)) {
                inv.setStatus(InvoiceStatus.overdue);
                repository.save(inv);
                updated++;
            }
        }
        log.info("Marked {} invoices as overdue (checked {} candidates)", updated, candidates.size());
    }
}