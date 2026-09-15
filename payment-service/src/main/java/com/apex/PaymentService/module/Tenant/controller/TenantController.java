package com.apex.PaymentService.module.Tenant.controller;

import com.apex.PaymentService.module.Tenant.dto.request.TenantCreateDto;
import com.apex.PaymentService.module.Tenant.dto.request.TenantUpdateDto;
import com.apex.PaymentService.module.Tenant.dto.response.TenantResponseDto;
import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import com.apex.PaymentService.module.Tenant.service.TenantService;
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
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService service;

    @PostMapping
    public ResponseEntity<TenantResponseDto> create(@Valid @RequestBody TenantCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTenant(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTenant(id));
    }

    @GetMapping("/email")
    public ResponseEntity<TenantResponseDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getTenantByEmail(email));
    }

    @GetMapping
    public ResponseEntity<Page<TenantResponseDto>> list(
            @RequestParam(required = false) TenantStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listTenants(status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TenantResponseDto> update(@PathVariable UUID id,
                                                    @Valid @RequestBody TenantUpdateDto dto) {
        return ResponseEntity.ok(service.updateTenant(id, dto));
    }

    // --- PUT method for full replacement (all fields required) ---
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponseDto> updateFull(@PathVariable UUID id,
                                                        @Valid @RequestBody TenantCreateDto dto) {
        return ResponseEntity.ok(service.updateFull(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
