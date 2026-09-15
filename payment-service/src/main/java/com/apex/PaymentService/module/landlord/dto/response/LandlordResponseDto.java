package com.apex.PaymentService.module.landlord.dto.response;

import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LandlordResponseDto {
    private UUID id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private LandlordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
