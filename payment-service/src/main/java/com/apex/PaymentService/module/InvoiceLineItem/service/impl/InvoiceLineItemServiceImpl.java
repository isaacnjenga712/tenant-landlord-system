package com.apex.PaymentService.module.InvoiceLineItem.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;

import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemCreateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemUpdateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.response.InvoiceLineItemResponseDto;
import com.apex.PaymentService.module.InvoiceLineItem.entity.InvoiceLineItem;
import com.apex.PaymentService.module.InvoiceLineItem.mapper.InvoiceLineItemMapper;
import com.apex.PaymentService.module.InvoiceLineItem.repository.InvoiceLineItemRepository;
import com.apex.PaymentService.module.InvoiceLineItem.service.InvoiceLineItemService;
import com.apex.PaymentService.module.Invoice.service.InvoiceService; // the real one
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
    private final InvoiceService invoiceService; // to validate invoice existence

    @Override
    @Transactional
    public InvoiceLineItemResponseDto createItem(InvoiceLineItemCreateDto dto) {
        // Validate invoice exists
        if (!invoiceService.exists(dto.getInvoiceId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId());
        }

        InvoiceLineItem item = mapper.toEntity(dto);
        item.calculateTotal(); // compute total from unitPrice * quantity + tax
        InvoiceLineItem saved = repository.save(item);
        log.info("Created line item {} for invoice {}", saved.getId(), saved.getInvoiceId());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceLineItemResponseDto getItem(UUID id) {
        InvoiceLineItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Line item not found"));
        return mapper.toResponseDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceLineItemResponseDto> getItemsByInvoice(UUID invoiceId) {
        return repository.findByInvoiceId(invoiceId)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceLineItemResponseDto> getItemsByInvoicePaged(UUID invoiceId, Pageable pageable) {
        return repository.findByInvoiceId(invoiceId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public InvoiceLineItemResponseDto updateItem(UUID id, InvoiceLineItemUpdateDto dto) {
        InvoiceLineItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Line item not found"));

        // Capture old values for potential recalc
        mapper.updateEntity(dto, item);

        // Recalculate total if any relevant field changed
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
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Line item not found");
        }
        repository.deleteById(id);
        log.info("Deleted line item {}", id);
    }

    @Override
    @Transactional
    public void deleteAllItemsByInvoice(UUID invoiceId) {
        // Optional: check invoice exists
        if (!invoiceService.exists(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        repository.deleteByInvoiceId(invoiceId);
        log.info("Deleted all line items for invoice {}", invoiceId);
    }
}
