package com.apex.PropertyManagementService.controllers;

import com.apex.PropertyManagementService.DTOs.request.UnitCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.UnitUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.UnitResponse;
import com.apex.PropertyManagementService.Service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<UnitResponse> createUnit(
            @Valid @RequestBody UnitCreateRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unitService.createUnit(request, tenantHeader));
    }

    @GetMapping
    public ResponseEntity<List<UnitResponse>> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> getUnitById(@PathVariable UUID id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @GetMapping("/unit-number/{unitNumber}")
    public ResponseEntity<UnitResponse> getUnitByUnitNumber(@PathVariable String unitNumber) {
        return ResponseEntity.ok(unitService.getUnitByUnitNumber(unitNumber));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<UnitResponse>> getUnitsByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(unitService.getUnitsByProperty(propertyId));
    }

    @GetMapping("/property/{propertyId}/available")
    public ResponseEntity<List<UnitResponse>> getAvailableUnitsByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(unitService.getAvailableUnitsByProperty(propertyId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<UnitResponse>> getUnitsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(unitService.getUnitsByStatus(status));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<UnitResponse>> getUnitsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(unitService.getUnitsByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitResponse> updateUnit(
            @PathVariable UUID id,
            @Valid @RequestBody UnitUpdateRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(unitService.updateUnit(id, request, tenantHeader));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UnitResponse> updateUnitStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(unitService.updateUnitStatus(id, status, tenantHeader));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<UnitResponse> assignTenant(
            @PathVariable UUID id,
            @RequestParam UUID tenantId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(unitService.assignTenant(id, tenantId, tenantHeader));
    }

    @PatchMapping("/{id}/vacate")
    public ResponseEntity<UnitResponse> vacateUnit(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(unitService.vacateUnit(id, tenantHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        unitService.deleteUnit(id, tenantHeader);
        return ResponseEntity.noContent().build();
    }
}