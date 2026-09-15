package com.apex.module.lease.controller;

import com.apex.module.lease.dto.request.LeaseCreateRequest;
import com.apex.module.lease.dto.request.LeaseUpdateRequest;
import com.apex.module.lease.service.LeaseService;
import com.apex.module.lease.dto.response.LeaseListResponse;
import com.apex.module.lease.dto.response.LeaseResponse;
import com.apex.module.lease.enums.LeaseStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leases")
@Validated
public class LeaseController {
	
	 private final LeaseService leaseService;

	LeaseController(LeaseService leaseService) {
		this.leaseService = leaseService;
	}

	    @PostMapping
	    public ResponseEntity<LeaseResponse> createLease(@Valid @RequestBody LeaseCreateRequest request) {
	        LeaseResponse response = leaseService.createLease(request);
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    }

	    @GetMapping("/{id}")
	    public ResponseEntity<LeaseResponse> getLease(@PathVariable UUID id) {
	        LeaseResponse response = leaseService.getLease(id);
	        return ResponseEntity.ok(response);
	    }

	    @PutMapping("/{id}")
	    public ResponseEntity<LeaseResponse> updateLease(@PathVariable UUID id,
	                                                     @Valid @RequestBody LeaseUpdateRequest request) {
	        LeaseResponse response = leaseService.updateLease(id, request);
	        return ResponseEntity.ok(response);
	    }

	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> cancelLease(@PathVariable UUID id) {
	        leaseService.cancelLease(id);
	        return ResponseEntity.noContent().build();
	    }

	    @PostMapping("/{id}/terminate")
	    public ResponseEntity<LeaseResponse> terminateLease(@PathVariable UUID id,
	                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminationDate) {
	        leaseService.terminateLease(id, terminationDate);
	        // Return the updated lease
	        LeaseResponse response = leaseService.getLease(id);
	        return ResponseEntity.ok(response);
	    }

	    @PostMapping("/{id}/renew")
	    public ResponseEntity<LeaseResponse> renewLease(@PathVariable UUID id,
	                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {
	        LeaseResponse response = leaseService.renewLease(id, newEndDate);
	        return ResponseEntity.ok(response);
	    }

	    @GetMapping
	    public ResponseEntity<LeaseListResponse> listLeases(
	            @RequestParam(required = false) UUID tenantId,
	            @RequestParam(required = false) UUID landlordId,
	            @RequestParam(required = false) UUID propertyId,
	            @RequestParam(required = false) LeaseStatus status,
	            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	            @RequestParam(defaultValue = "0") @Min(0) int page,
	            @RequestParam(defaultValue = "20") @Min(1) int size) {

	        LeaseListResponse response = leaseService.listLeases(
	                tenantId, landlordId, propertyId, status, startDate, endDate, page, size);
	        return ResponseEntity.ok(response);
	    }

}
