package com.apex.PaymentService.module.PaymentSplit.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import com.apex.PaymentService.module.Payment.service.PaymentService;
import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitCreateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitUpdateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.response.PaymentSplitResponseDto;
import com.apex.PaymentService.module.PaymentSplit.entity.PaymentSplit;
import com.apex.PaymentService.module.PaymentSplit.mapper.PaymentSplitMapper;
import com.apex.PaymentService.module.PaymentSplit.repository.PaymentSplitRepository;
import com.apex.PaymentService.module.PaymentSplit.service.PaymentSplitService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSplitServiceImpl implements PaymentSplitService {

    private final PaymentSplitRepository repository;
    private final PaymentSplitMapper mapper;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public PaymentSplitResponseDto createSplit(PaymentSplitCreateDto dto) {
        UUID orgId = TenantContext.require();

        if (!paymentService.exists(dto.getPaymentId())) {
            throw new ResourceNotFoundException("Payment not found with id: " + dto.getPaymentId());
        }
        if (!invoiceService.exists(dto.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId());
        }

        BigDecimal paymentAmount = paymentService.getPaymentAmount(dto.getPaymentId());
        BigDecimal currentAllocated = repository.sumAllocatedAmountByOrgIdAndPaymentId(orgId, dto.getPaymentId());
        BigDecimal newTotal = currentAllocated.add(dto.getAllocatedAmount());
        if (newTotal.compareTo(paymentAmount) > 0) {
            throw new BusinessException("Total allocated amount (" + newTotal +
                    ") exceeds payment amount (" + paymentAmount + ")");
        }

        PaymentSplit split = mapper.toEntity(dto);
        split.setOrgId(orgId);
        PaymentSplit saved = repository.save(split);
        log.info("Created payment split for payment {} ({} allocated) org {}",
                saved.getPaymentId(), saved.getAllocatedAmount(), orgId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSplitResponseDto getSplit(UUID id) {
        UUID orgId = TenantContext.require();
        PaymentSplit split = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment split not found"));
        return mapper.toResponseDto(split);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentSplitResponseDto> getSplitsByPayment(UUID paymentId) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndPaymentId(orgId, paymentId)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSplitResponseDto> getSplitsByPaymentPaged(UUID paymentId, Pageable pageable) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndPaymentId(orgId, paymentId, pageable)
                .map(mapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSplitResponseDto> getSplitsByInvoicePaged(UUID invoiceId, Pageable pageable) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndInvoiceId(orgId, invoiceId, pageable)
                .map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public PaymentSplitResponseDto updateSplit(UUID id, PaymentSplitUpdateDto dto) {
        UUID orgId = TenantContext.require();
        PaymentSplit split = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment split not found"));

        if (dto.getInvoiceId() != null && !dto.getInvoiceId().equals(split.getInvoiceId())) {
            if (!invoiceService.exists(dto.getInvoiceId())) {
                throw new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId());
            }
            split.setInvoiceId(dto.getInvoiceId());
        }

        if (dto.getAllocatedAmount() != null && !dto.getAllocatedAmount().equals(split.getAllocatedAmount())) {
            BigDecimal oldAmount = split.getAllocatedAmount();
            BigDecimal paymentAmount = paymentService.getPaymentAmount(split.getPaymentId());
            BigDecimal currentAllocated = repository.sumAllocatedAmountByOrgIdAndPaymentId(orgId, split.getPaymentId());
            BigDecimal newTotal = currentAllocated.subtract(oldAmount).add(dto.getAllocatedAmount());
            if (newTotal.compareTo(paymentAmount) > 0) {
                throw new BusinessException("Total allocated amount (" + newTotal +
                        ") exceeds payment amount (" + paymentAmount + ")");
            }
            split.setAllocatedAmount(dto.getAllocatedAmount());
        }

        mapper.updateEntity(dto, split);
        PaymentSplit updated = repository.save(split);
        log.info("Updated payment split {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteSplit(UUID id) {
        UUID orgId = TenantContext.require();
        if (!repository.existsByIdAndOrgId(id, orgId)) {
            throw new ResourceNotFoundException("Payment split not found");
        }
        repository.deleteById(id);
        log.info("Deleted payment split {}", id);
    }

    @Override
    @Transactional
    public void deleteAllSplitsByPayment(UUID paymentId) {
        UUID orgId = TenantContext.require();
        if (!paymentService.exists(paymentId)) {
            throw new ResourceNotFoundException("Payment not found with id: " + paymentId);
        }
        List<PaymentSplit> splits = repository.findByOrgIdAndPaymentId(orgId, paymentId);
        if (!splits.isEmpty()) {
            repository.deleteAll(splits);
            log.info("Deleted {} splits for payment {} org {}", splits.size(), paymentId, orgId);
        }
    }
}