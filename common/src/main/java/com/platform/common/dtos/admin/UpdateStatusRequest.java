package com.platform.common.dtos.admin;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "active must not be null")
        Boolean active
) {}
