package com.apex.maintenanceTicketService.dto.request;

import com.apex.maintenanceTicketService.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketCreateRequest {

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    /**
     * Set when a TENANT creates the ticket.
     * The frontend supplies this from the tenant's active lease.
     */
    private UUID landlordId;

    /**
     * Set when a LANDLORD creates the ticket on behalf of a tenant.
     * Ignored when caller is TENANT (server derives it from X-User-Id).
     */
    private UUID tenantId;
}