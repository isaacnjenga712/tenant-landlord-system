package com.apex.PaymentService.module.Invoice.controller;

import com.apex.PaymentService.module.Invoice.dto.request.InvoiceCreateDto;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceUpdateDto;
import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService service;

    // ---------- CRUD Endpoints ----------

    @PostMapping
    public ResponseEntity<InvoiceResponseDto> create(@Valid @RequestBody InvoiceCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInvoice(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getInvoice(id));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponseDto> getByNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(service.getInvoiceByNumber(invoiceNumber));
    }

    /**
     * Lists invoices with optional filters and pagination.
     * Caller identity (X-User-Id / X-User-Role) is accepted and logged; frontend
     * is responsible for passing the correct leaseId filter today.
     */
    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDto>> list(
            @RequestParam(required = false) UUID leaseId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("listInvoices: leaseId={} status={} caller={} role={}",
                leaseId, status, userHeader, userRole);

        return ResponseEntity.ok(service.listInvoices(leaseId, status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InvoiceResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceUpdateDto dto) {
        return ResponseEntity.ok(service.updateInvoice(id, dto));
    }

    @PatchMapping("/{id}/void")
    public ResponseEntity<Void> voidInvoice(@PathVariable UUID id) {
        service.voidInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Payment Integration Endpoints ----------

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponseDto> applyPayment(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(service.applyPaymentAndGetInvoice(id, amount));
    }

    @PatchMapping("/{id}/late-fee")
    public ResponseEntity<Void> applyLateFee(
            @PathVariable UUID id,
            @RequestParam BigDecimal feeAmount) {
        service.applyLateFee(id, feeAmount);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-overdue")
    public ResponseEntity<Void> markOverdue() {
        service.markOverdueInvoices();
        return ResponseEntity.noContent().build();
    }
}