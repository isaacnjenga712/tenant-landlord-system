package com.apex.PaymentService.module.Payment.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import com.apex.PaymentService.module.Payment.dto.request.PaymentCreateDto;
import com.apex.PaymentService.module.Payment.dto.request.PaymentUpdateDto;
import com.apex.PaymentService.module.Payment.dto.response.PaymentResponseDto;
import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import com.apex.PaymentService.module.Payment.kafka.KafkaProducer;   // ✅ added
import com.apex.PaymentService.module.Payment.mapper.PaymentMapper;
import com.apex.PaymentService.module.Payment.repository.PaymentRepository;
import com.apex.PaymentService.module.Payment.service.PaymentService;
import com.apex.PaymentService.module.Tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final InvoiceService invoiceService;
    private final TenantService tenantService;
    private final KafkaProducer kafkaProducer;   // ✅ added

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentCreateDto dto) {
        if (!tenantService.exists(dto.getTenantId())) {
            throw new ResourceNotFoundException("Tenant not found with id: " + dto.getTenantId());
        }

        if (dto.getInvoiceId() != null && !invoiceService.exists(dto.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId());
        }

        if (dto.getGatewayTransactionId() != null &&
                repository.findByGatewayTransactionId(dto.getGatewayTransactionId()).isPresent()) {
            throw new BusinessException("Duplicate gateway transaction ID");
        }

        Payment payment = mapper.toEntity(dto);
        Payment saved = repository.save(payment);
        log.info("Created payment {} for tenant {}", saved.getId(), saved.getTenantId());

        // ✅ publish event
        kafkaProducer.sendPaymentCreatedEvent(saved);

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(UUID id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> listPayments(UUID tenantId, UUID invoiceId, PaymentStatus status, Pageable pageable) {
        if (tenantId != null) {
            return repository.findByTenantId(tenantId, pageable).map(mapper::toResponseDto);
        }
        if (invoiceId != null) {
            return repository.findByInvoiceId(invoiceId, pageable).map(mapper::toResponseDto);
        }
        if (status != null) {
            return repository.findByStatus(status, pageable).map(mapper::toResponseDto);
        }
        return repository.findAll(pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePayment(UUID id, PaymentUpdateDto dto) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.refunded) {
            throw new BusinessException("Cannot update a refunded payment");
        }

        if (dto.getInvoiceId() != null && !invoiceService.exists(dto.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found");
        }

        mapper.updateEntity(dto, payment);
        Payment updated = repository.save(payment);
        log.info("Updated payment {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deletePayment(UUID id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.initiated && payment.getStatus() != PaymentStatus.failed) {
            throw new BusinessException("Cannot delete payment with status: " + payment.getStatus());
        }
        repository.deleteById(id);
        log.info("Deleted payment {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPaymentAmount(UUID paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return payment.getAmount();
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(UUID id, PaymentStatus newStatus, String gatewayResponse) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.refunded) {
            throw new BusinessException("Cannot change status of refunded payment");
        }

        payment.setStatus(newStatus);
        payment.setProcessedAt(LocalDateTime.now());
        if (gatewayResponse != null) {
            payment.setGatewayResponse(gatewayResponse);
        }

        if (newStatus == PaymentStatus.success && payment.getInvoiceId() != null && !payment.getAppliedToInvoice()) {
            invoiceService.applyPayment(payment.getInvoiceId(), payment.getAmount());
            payment.setAppliedToInvoice(true);
        }

        Payment updated = repository.save(payment);
        log.info("Processed payment {} with status {}", updated.getId(), newStatus);

        // ✅ publish event if successful
        if (newStatus == PaymentStatus.success) {
            kafkaProducer.sendPaymentProcessedEvent(updated);
        }

        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(UUID id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.success) {
            throw new BusinessException("Only successful payments can be refunded");
        }
        if (payment.getAppliedToInvoice() && payment.getInvoiceId() != null) {
            invoiceService.reversePayment(payment.getInvoiceId(), payment.getAmount());
        }
        payment.markRefunded();
        Payment updated = repository.save(payment);
        log.info("Refunded payment {}", updated.getId());

        // ✅ publish event
        kafkaProducer.sendPaymentRefundedEvent(updated);

        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void applyPaymentToInvoice(UUID paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.success) {
            throw new BusinessException("Cannot apply non-successful payment");
        }
        if (payment.getAppliedToInvoice()) {
            throw new BusinessException("Payment already applied");
        }
        invoiceService.applyPayment(payment.getInvoiceId(), payment.getAmount());
        payment.setAppliedToInvoice(true);
        repository.save(payment);
        log.info("Applied payment {} to invoice {}", paymentId, payment.getInvoiceId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID paymentId) {
        return repository.existsById(paymentId);
    }
}