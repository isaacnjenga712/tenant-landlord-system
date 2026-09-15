package com.apex.maintenanceTicketService.dto.response;


import com.apex.maintenanceTicketService.model.enums.TicketPriority;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketResponse {
    private UUID id;
    private UUID unitId;
    private UUID tenantId;
    private UUID landlordId;
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}