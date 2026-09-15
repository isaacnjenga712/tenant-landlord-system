package com.apex.PaymentService.module.Invoice.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceCreateDto;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceUpdateDto;
import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
import com.apex.PaymentService.module.Invoice.entity.Invoice;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import com.apex.PaymentService.module.Invoice.mapper.InvoiceMapper;
import com.apex.PaymentService.module.Invoice.repository.InvoiceRepository;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import com.apex.PaymentService.module.Invoice.service.external.LeaseValidationService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final LeaseValidationService leaseValidationService;

    // ---------- CRUD ----------

    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceCreateDto dto) {
        if (!leaseValidationService.exists(dto.getLeaseId())) {
            throw new ResourceNotFoundException("Lease not found with id: " + dto.getLeaseId());
        }
        if (repository.findByInvoiceNumber(dto.getInvoiceNumber()).isPresent()) {
            throw new BusinessException("Invoice number already exists: " + dto.getInvoiceNumber());
        }
        Invoice invoice = mapper.toEntity(dto);
        invoice.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO);
        invoice.updateStatus();
        Invoice saved = repository.save(invoice);
        log.info("Created invoice: {} for lease: {}", saved.getInvoiceNumber(), saved.getLeaseId());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoice(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return mapper.toResponseDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = repository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
        return mapper.toResponseDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> listInvoices(UUID leaseId, InvoiceStatus status, Pageable pageable) {
        if (leaseId != null && status != null) {
            return repository.findByLeaseIdAndStatus(leaseId, status, pageable)
                    .map(mapper::toResponseDto);
        } else if (leaseId != null) {
            return repository.findByLeaseId(leaseId, pageable)
                    .map(mapper::toResponseDto);
        } else if (status != null) {
            return repository.findByStatus(status, pageable)
                    .map(mapper::toResponseDto);
        } else {
            return repository.findAll(pageable).map(mapper::toResponseDto);
        }
    }

    @Override
    @Transactional
    public InvoiceResponseDto updateInvoice(UUID id, InvoiceUpdateDto dto) {
        Invoice invoice = repository.findById(id)
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
        Invoice invoice = repository.findById(id)
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
        return repository.existsById(invoiceId);
    }

    /**
     * Applies a payment to the invoice (void method – used by Payment module).
     * Uses optimistic locking retry.
     */
    @Override
    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void applyPayment(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.applyPayment(amount);
        repository.save(invoice);
        log.info("Applied payment of {} to invoice {}", amount, invoiceId);
    }

    /**
     * Reverses a payment on the invoice (void method – used by Payment module).
     * Uses optimistic locking retry.
     */
    @Override
    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void reversePayment(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.reversePayment(amount);
        repository.save(invoice);
        log.info("Reversed payment of {} on invoice {}", amount, invoiceId);
    }

    /**
     * Applies a payment and returns the updated invoice DTO – used by the controller.
     */
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
        Invoice invoice = repository.findById(id)
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
        LocalDate today = LocalDate.now();
        int updated = repository.markOverdueInvoices(today);
        log.info("Marked {} invoices as overdue", updated);
    }
}
