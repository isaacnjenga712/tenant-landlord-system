package com.apex.mpesa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StkPushRequestDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone number must be in format 254XXXXXXXXX")
    private String phone;

    @NotNull(message = "Amount is required")
    private Integer amount;

    @NotBlank(message = "Account reference is required")
    private String accountReference;

    @NotBlank(message = "Transaction description is required")
    private String transactionDesc;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Lease ID is required")
    private String leaseId;

    private String invoiceId;
}
