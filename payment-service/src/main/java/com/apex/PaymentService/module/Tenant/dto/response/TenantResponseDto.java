package com.apex.PaymentService.module.Tenant.dto.response;

import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TenantResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private TenantStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}