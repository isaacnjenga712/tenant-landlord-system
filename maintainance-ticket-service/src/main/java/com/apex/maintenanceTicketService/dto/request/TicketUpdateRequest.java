package com.apex.maintenanceTicketService.dto.request;

import com.apex.maintenanceTicketService.model.enums.TicketPriority;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import lombok.Data;

@Data
public class TicketUpdateRequest {
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
}