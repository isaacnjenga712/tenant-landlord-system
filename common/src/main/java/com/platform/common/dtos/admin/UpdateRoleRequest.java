package com.platform.common.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateRoleRequest(
        @NotBlank
        @Pattern(
                regexp = "TENANT|LANDLORD|ADMIN",
                message = "role must be one of TENANT, LANDLORD, ADMIN"
        )
        String role
) {}