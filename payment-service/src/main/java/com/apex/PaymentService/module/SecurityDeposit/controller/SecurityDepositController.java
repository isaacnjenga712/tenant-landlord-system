package com.apex.PaymentService.module.SecurityDeposit.controller;

import com.apex.PaymentService.module.SecurityDeposit.dto.request.DeductionRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.ReturnDepositRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositCreateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositUpdateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.response.SecurityDepositResponseDto;
import com.apex.PaymentService.module.SecurityDeposit.service.SecurityDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/security-deposits")
@RequiredArgsConstructor
public class SecurityDepositController {

    private final SecurityDepositService service;

    @PostMapping
    public ResponseEntity<SecurityDepositResponseDto> create(@Valid @RequestBody SecurityDepositCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDeposit(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecurityDepositResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getDeposit(id));
    }

    @GetMapping("/by-lease/{leaseId}")
    public ResponseEntity<SecurityDepositResponseDto> getByLease(@PathVariable UUID leaseId) {
        return ResponseEntity.ok(service.getDepositByLease(leaseId));
    }

    @GetMapping
    public ResponseEntity<Page<SecurityDepositResponseDto>> list(
            @RequestParam(required = false) UUID leaseId,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listDeposits(leaseId, activeOnly, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SecurityDepositResponseDto> update(@PathVariable UUID id,
                                                             @Valid @RequestBody SecurityDepositUpdateDto dto) {
        return ResponseEntity.ok(service.updateDeposit(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteDeposit(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deduction")
    public ResponseEntity<SecurityDepositResponseDto> addDeduction(@PathVariable UUID id,
                                                                   @Valid @RequestBody DeductionRequestDto dto) {
        return ResponseEntity.ok(service.addDeduction(id, dto));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<SecurityDepositResponseDto> returnDeposit(@PathVariable UUID id,
                                                                    @Valid @RequestBody ReturnDepositRequestDto dto) {
        return ResponseEntity.ok(service.returnDeposit(id, dto));
    }

    @PatchMapping("/{id}/interest")
    public ResponseEntity<Void> accrueInterest(@PathVariable UUID id,
                                               @RequestParam BigDecimal interestRate) {
        service.accrueInterest(id, interestRate);
        return ResponseEntity.noContent().build();
    }
}
