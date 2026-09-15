package com.apex.maintenanceTicketService.controller;


import com.apex.maintenanceTicketService.dto.request.TenantCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TenantUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TenantResponse;
import com.apex.maintenanceTicketService.service.TenantCommandService;
import com.apex.maintenanceTicketService.service.TenantQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantCommandService commandService;
    private final TenantQueryService queryService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createTenant(request));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(queryService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.getTenantById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<TenantResponse> getTenantByEmail(@PathVariable String email) {
        return ResponseEntity.ok(queryService.getTenantByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody TenantUpdateRequest request) {
        return ResponseEntity.ok(commandService.updateTenant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        commandService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}