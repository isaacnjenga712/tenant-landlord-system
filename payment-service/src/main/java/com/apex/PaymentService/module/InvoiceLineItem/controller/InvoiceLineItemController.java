package com.apex.PaymentService.module.InvoiceLineItem.controller;

import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemCreateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemUpdateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.response.InvoiceLineItemResponseDto;
import com.apex.PaymentService.module.InvoiceLineItem.service.InvoiceLineItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice-line-items")
@RequiredArgsConstructor
public class InvoiceLineItemController {

    private final InvoiceLineItemService service;

    @PostMapping
    public ResponseEntity<InvoiceLineItemResponseDto> create(@Valid @RequestBody InvoiceLineItemCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceLineItemResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getItem(id));
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<List<InvoiceLineItemResponseDto>> getByInvoice(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(service.getItemsByInvoice(invoiceId));
    }

    @GetMapping("/by-invoice-paged/{invoiceId}")
    public ResponseEntity<Page<InvoiceLineItemResponseDto>> getByInvoicePaged(
            @PathVariable UUID invoiceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getItemsByInvoicePaged(invoiceId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InvoiceLineItemResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceLineItemUpdateDto dto) {
        return ResponseEntity.ok(service.updateItem(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<Void> deleteAllByInvoice(@PathVariable UUID invoiceId) {
        service.deleteAllItemsByInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }
}
