package com.apex.PaymentService.module.landlord.controller;

import com.apex.PaymentService.module.landlord.dto.request.LandlordCreateDto;
import com.apex.PaymentService.module.landlord.dto.request.LandlordUpdateDto;
import com.apex.PaymentService.module.landlord.dto.response.LandlordResponseDto;
import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import com.apex.PaymentService.module.landlord.service.LandlordService;
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
@RequestMapping("/api/v1/landlords")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService service;

    @PostMapping
    public ResponseEntity<LandlordResponseDto> create(@Valid @RequestBody LandlordCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLandlord(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandlordResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getLandlord(id));
    }

    @GetMapping("/email")
    public ResponseEntity<LandlordResponseDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getLandlordByEmail(email));
    }

    @GetMapping
    public ResponseEntity<Page<LandlordResponseDto>> list(
            @RequestParam(required = false) LandlordStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listLandlords(status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LandlordResponseDto> update(@PathVariable UUID id,
                                                      @Valid @RequestBody LandlordUpdateDto dto) {
        return ResponseEntity.ok(service.updateLandlord(id, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandlordResponseDto> updateFull(@PathVariable UUID id,
                                                          @Valid @RequestBody LandlordCreateDto dto) {
        return ResponseEntity.ok(service.updateFull(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteLandlord(id);
        return ResponseEntity.noContent().build();
    }
}
