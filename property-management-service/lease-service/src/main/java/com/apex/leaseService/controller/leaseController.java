package com.apex.leaseService.controller;

import com.apex.leaseService.dtos.LeaseRequest;

import com.apex.leaseService.dtos.LeaseResponse;
import com.apex.leaseService.service.LeaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leases")
@RequiredArgsConstructor
public class leaseController {
	
	
	private final LeaseService leaseservice; // <-- This is the instance (lowercase l)
	//CREATE
	@PostMapping
    public ResponseEntity<LeaseResponse> createLease(@Valid @RequestBody LeaseRequest request) {
        return new ResponseEntity<>(leaseservice.createLease(request), HttpStatus.CREATED);
    }

    // READ - All
    @GetMapping
    public ResponseEntity<List<LeaseResponse>> getAllLeases() {
        return ResponseEntity.ok(leaseservice.getAllLeases());
    }

    // READ - By ID
    @GetMapping("/{id}")
    public ResponseEntity<LeaseResponse> getLeaseById(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseservice.getLeaseById(id));
    }

    // READ - By Unit
    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<LeaseResponse>> getLeasesByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(leaseservice.getLeasesByUnit(unitId));
    }

    // READ - By Tenant
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<LeaseResponse>> getLeasesByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(leaseservice.getLeasesByTenant(tenantId));
    }

    // UPDATE - Full Update
    @PutMapping("/{id}")
    public ResponseEntity<LeaseResponse> updateLease(@PathVariable UUID id, 
                                                      @Valid @RequestBody LeaseRequest request) {
        return ResponseEntity.ok(leaseservice.updateLease(id, request));
    }

    // PATCH - Update Status
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaseResponse> updateLeaseStatus(@PathVariable UUID id, 
                                                           @RequestParam String status) {
        return ResponseEntity.ok(leaseservice.updateLeaseStatus(id, status));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable UUID id) {
        leaseservice.deleteLease(id);
        return ResponseEntity.noContent().build();
	}
}
