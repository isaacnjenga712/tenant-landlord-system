package com.apex.maintenanceTicketService.dto.request;

import com.apex.maintenanceTicketService.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketCreateRequest {

    @NotNull(message = "Unit ID is required")
    private UUID unitId;          // ✅ MUST be UUID, not String

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}