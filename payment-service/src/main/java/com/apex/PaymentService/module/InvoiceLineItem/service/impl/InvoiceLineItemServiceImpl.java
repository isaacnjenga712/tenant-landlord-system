package com.apex.PaymentService.module.InvoiceLineItem.service.impl;

import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemCreateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemUpdateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.response.InvoiceLineItemResponseDto;
import com.apex.PaymentService.module.InvoiceLineItem.entity.InvoiceLineItem;
import com.apex.PaymentService.module.InvoiceLineItem.mapper.InvoiceLineItemMapper;
import com.apex.PaymentService.module.InvoiceLineItem.repository.InvoiceLineItemRepository;
import com.apex.PaymentService.module.InvoiceLineItem.service.InvoiceLineItemService;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceLineItemServiceImpl implements InvoiceLineItemService {

    private final InvoiceLineItemRepository repository;
    private final InvoiceLineItemMapper mapper;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public InvoiceLineItemResponseDto createItem(InvoiceLineItemCreateDto dto) {
        UUID orgId = TenantContext.require();

        if (!invoiceService.exists(dto.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId());
        }

        InvoiceLineItem item = mapper.toEntity(dto);
        item.setOrgId(orgId);
        item.calculateTotal();
        InvoiceLineItem saved = repository.save(item);
        log.info("Created line item {} for invoice {} org {}", saved.getId(), saved.getInvoiceId(), orgId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceLineItemResponseDto getItem(UUID id) {
        UUID orgId = TenantContext.require();
        InvoiceLineItem item = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Line item not found"));
        return mapper.toResponseDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceLineItemResponseDto> getItemsByInvoice(UUID invoiceId) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndInvoiceId(orgId, invoiceId)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceLineItemResponseDto> getItemsByInvoicePaged(UUID invoiceId, Pageable pageable) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndInvoiceId(orgId, invoiceId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public InvoiceLineItemResponseDto updateItem(UUID id, InvoiceLineItemUpdateDto dto) {
        UUID orgId = TenantContext.require();
        InvoiceLineItem item = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Line item not found"));

        mapper.updateEntity(dto, item);

        if (dto.getQuantity() != null || dto.getUnitPrice() != null || dto.getTaxRate() != null) {
            item.calculateTotal();
        }

        InvoiceLineItem updated = repository.save(item);
        log.info("Updated line item {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteItem(UUID id) {
        UUID orgId = TenantContext.require();
        if (!repository.existsByIdAndOrgId(id, orgId)) {
            throw new ResourceNotFoundException("Line item not found");
        }
        repository.deleteById(id);
        log.info("Deleted line item {}", id);
    }

    @Override
    @Transactional
    public void deleteAllItemsByInvoice(UUID invoiceId) {
        UUID orgId = TenantContext.require();
        if (!invoiceService.exists(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        repository.deleteByOrgIdAndInvoiceId(orgId, invoiceId);
        log.info("Deleted all line items for invoice {} org {}", invoiceId, orgId);
    }
}