package com.apex.PaymentService.module.DepositDeduction.controller;

import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionCreateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionUpdateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.response.DepositDeductionResponseDto;
import com.apex.PaymentService.module.DepositDeduction.service.DepositDeductionService;
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
@RequestMapping("/api/v1/deposit-deductions")
@RequiredArgsConstructor
public class DepositDeductionController {

    private final DepositDeductionService service;

    @PostMapping
    public ResponseEntity<DepositDeductionResponseDto> create(@Valid @RequestBody DepositDeductionCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDeduction(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositDeductionResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getDeduction(id));
    }

    @GetMapping("/by-deposit/{depositId}")
    public ResponseEntity<List<DepositDeductionResponseDto>> getByDeposit(@PathVariable UUID depositId) {
        return ResponseEntity.ok(service.getDeductionsByDeposit(depositId));
    }

    @GetMapping("/by-deposit-paged/{depositId}")
    public ResponseEntity<Page<DepositDeductionResponseDto>> getByDepositPaged(
            @PathVariable UUID depositId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getDeductionsByDepositPaged(depositId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DepositDeductionResponseDto> update(@PathVariable UUID id,
                                                              @Valid @RequestBody DepositDeductionUpdateDto dto) {
        return ResponseEntity.ok(service.updateDeduction(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteDeduction(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-deposit/{depositId}")
    public ResponseEntity<Void> deleteAllByDeposit(@PathVariable UUID depositId) {
        service.deleteAllDeductionsByDeposit(depositId);
        return ResponseEntity.noContent().build();
    }
}
