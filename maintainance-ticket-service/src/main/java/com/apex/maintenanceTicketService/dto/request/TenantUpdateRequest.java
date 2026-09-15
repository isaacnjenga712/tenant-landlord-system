package com.apex.maintenanceTicketService.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class TenantUpdateRequest {
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    private String phone;
}