package com.apex.PaymentService.module.PaymentMethod.controller;

import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodCreateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodUpdateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.response.PaymentMethodResponseDto;
import com.apex.PaymentService.module.PaymentMethod.service.PaymentMethodService;
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
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService service;

    @PostMapping
    public ResponseEntity<PaymentMethodResponseDto> create(@Valid @RequestBody PaymentMethodCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPaymentMethod(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getPaymentMethod(id));
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<Page<PaymentMethodResponseDto>> getByTenant(
            @PathVariable UUID tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getPaymentMethodsByTenant(tenantId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> update(@PathVariable UUID id,
                                                           @Valid @RequestBody PaymentMethodUpdateDto dto) {
        return ResponseEntity.ok(service.updatePaymentMethod(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deletePaymentMethod(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<PaymentMethodResponseDto> setDefault(@PathVariable UUID id) {
        return ResponseEntity.ok(service.setDefaultMethod(id));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PaymentMethodResponseDto> toggleActive(@PathVariable UUID id,
                                                                 @RequestParam boolean active) {
        return ResponseEntity.ok(service.toggleActive(id, active));
    }
}
