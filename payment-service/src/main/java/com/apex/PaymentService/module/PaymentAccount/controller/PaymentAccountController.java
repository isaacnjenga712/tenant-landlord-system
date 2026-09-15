package com.apex.PaymentService.module.PaymentAccount.controller;

import com.apex.PaymentService.module.PaymentAccount.dto.request.BalanceAdjustmentDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountCreateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountUpdateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.TransferRequestDto;
import com.apex.PaymentService.module.PaymentAccount.dto.response.PaymentAccountResponseDto;
import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import com.apex.PaymentService.module.PaymentAccount.service.PaymentAccountService;
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
@RequestMapping("/api/v1/payment-accounts")
@RequiredArgsConstructor
public class PaymentAccountController {

    private final PaymentAccountService service;

    @PostMapping
    public ResponseEntity<PaymentAccountResponseDto> create(@Valid @RequestBody PaymentAccountCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentAccountResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAccount(id));
    }

    @GetMapping("/by-entity")
    public ResponseEntity<PaymentAccountResponseDto> getByEntity(
            @RequestParam EntityType entityType,
            @RequestParam UUID entityId) {
        return ResponseEntity.ok(service.getAccountByEntity(entityType, entityId));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentAccountResponseDto>> list(
            @RequestParam(required = false) EntityType entityType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listAccounts(entityType, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentAccountResponseDto> update(@PathVariable UUID id,
                                                            @Valid @RequestBody PaymentAccountUpdateDto dto) {
        return ResponseEntity.ok(service.updateAccount(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<PaymentAccountResponseDto> adjustBalance(@PathVariable UUID id,
                                                                   @Valid @RequestBody BalanceAdjustmentDto dto) {
        return ResponseEntity.ok(service.adjustBalance(id, dto));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<PaymentAccountResponseDto> transferBalance(@PathVariable UUID id,
                                                                     @Valid @RequestBody TransferRequestDto dto) {
        return ResponseEntity.ok(service.transferBalance(id, dto));
    }
}
