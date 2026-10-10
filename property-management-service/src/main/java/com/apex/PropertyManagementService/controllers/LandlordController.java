package com.apex.PropertyManagementService.controllers;

import com.apex.PropertyManagementService.DTOs.request.LandlordCreateRequest;

import com.apex.PropertyManagementService.DTOs.request.LandlordUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.LandlordResponse;
import com.apex.PropertyManagementService.Service.LandlordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/landlords")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;

    @PostMapping
    public ResponseEntity<LandlordResponse> createLandlord(@Valid @RequestBody LandlordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(landlordService.createLandlord(request));
    }

    @GetMapping
    public ResponseEntity<List<LandlordResponse>> getAllLandlords() {
        return ResponseEntity.ok(landlordService.getAllLandlords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandlordResponse> getLandlordById(@PathVariable UUID id) {
        return ResponseEntity.ok(landlordService.getLandlordById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandlordResponse> updateLandlord(
            @PathVariable UUID id,
            @Valid @RequestBody LandlordUpdateRequest request) {
        return ResponseEntity.ok(landlordService.updateLandlord(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLandlord(@PathVariable UUID id) {
        landlordService.deleteLandlord(id);
        return ResponseEntity.noContent().build();
    }
}