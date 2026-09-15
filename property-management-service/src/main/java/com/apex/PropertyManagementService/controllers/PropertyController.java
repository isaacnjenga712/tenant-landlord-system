package com.apex.PropertyManagementService.controllers;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;

import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.Service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

 

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyCreateRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyService.createProperty(request, tenantHeader));
    }

    
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @GetMapping("/property-id/{propertyId}")
    public ResponseEntity<PropertyResponse> getPropertyByPropertyId(@PathVariable String propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyByPropertyId(propertyId));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByLandlord(@PathVariable UUID landlordId) {
        return ResponseEntity.ok(propertyService.getPropertiesByLandlord(landlordId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(propertyService.getPropertiesByStatus(status));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByCity(@PathVariable String city) {
        return ResponseEntity.ok(propertyService.getPropertiesByCity(city));
    }

   

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody PropertyUpdateRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request, tenantHeader));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PropertyResponse> updatePropertyStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(propertyService.updatePropertyStatus(id, status, tenantHeader));
    }

  
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        propertyService.deleteProperty(id, tenantHeader);
        return ResponseEntity.noContent().build();
    }
}