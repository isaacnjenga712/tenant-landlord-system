package com.apex.maintenanceTicketService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TenantResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}