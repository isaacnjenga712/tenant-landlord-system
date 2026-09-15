package com.apex.PaymentService.module.PaymentSplit.controller;

import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitCreateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitUpdateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.response.PaymentSplitResponseDto;
import com.apex.PaymentService.module.PaymentSplit.service.PaymentSplitService;
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
@RequestMapping("/api/v1/payment-splits")
@RequiredArgsConstructor
public class PaymentSplitController {

    private final PaymentSplitService service;

    @PostMapping
    public ResponseEntity<PaymentSplitResponseDto> create(@Valid @RequestBody PaymentSplitCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSplit(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentSplitResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getSplit(id));
    }

    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<List<PaymentSplitResponseDto>> getByPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(service.getSplitsByPayment(paymentId));
    }

    @GetMapping("/by-payment-paged/{paymentId}")
    public ResponseEntity<Page<PaymentSplitResponseDto>> getByPaymentPaged(
            @PathVariable UUID paymentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getSplitsByPaymentPaged(paymentId, pageable));
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<Page<PaymentSplitResponseDto>> getByInvoicePaged(
            @PathVariable UUID invoiceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getSplitsByInvoicePaged(invoiceId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentSplitResponseDto> update(@PathVariable UUID id,
                                                          @Valid @RequestBody PaymentSplitUpdateDto dto) {
        return ResponseEntity.ok(service.updateSplit(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteSplit(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-payment/{paymentId}")
    public ResponseEntity<Void> deleteAllByPayment(@PathVariable UUID paymentId) {
        service.deleteAllSplitsByPayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
