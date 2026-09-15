package com.apex.PaymentService.module.Payment.controller;

import com.apex.PaymentService.module.Payment.dto.request.PaymentCreateDto;
import com.apex.PaymentService.module.Payment.dto.request.PaymentUpdateDto;
import com.apex.PaymentService.module.Payment.dto.response.PaymentResponseDto;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import com.apex.PaymentService.module.Payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> create(@Valid @RequestBody PaymentCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getPayment(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID invoiceId,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listPayments(tenantId, invoiceId, status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> update(@PathVariable UUID id,
                                                     @Valid @RequestBody PaymentUpdateDto dto) {
        return ResponseEntity.ok(service.updatePayment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @PathVariable UUID id,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String gatewayResponse) {
        return ResponseEntity.ok(service.processPayment(id, status, gatewayResponse));
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDto> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(service.refundPayment(id));
    }

    @PatchMapping("/{id}/apply")
    public ResponseEntity<Void> applyToInvoice(@PathVariable UUID id) {
        service.applyPaymentToInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
