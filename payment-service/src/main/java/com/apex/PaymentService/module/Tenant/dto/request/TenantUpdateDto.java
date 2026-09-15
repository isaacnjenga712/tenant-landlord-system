package com.apex.PaymentService.module.Tenant.dto.request;

import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TenantUpdateDto {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    private LocalDate dateOfBirth;

    private TenantStatus status;
}
