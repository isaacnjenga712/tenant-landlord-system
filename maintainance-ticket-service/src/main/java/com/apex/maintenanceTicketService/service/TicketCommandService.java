package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.request.TicketCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TicketUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TicketResponse;

import java.util.UUID;

public interface TicketCommandService {

    // ✅ tenantId and landlordId are UUID
    TicketResponse createTicket(TicketCreateRequest request, UUID tenantId, UUID landlordId);

    TicketResponse updateTicket(UUID id, TicketUpdateRequest request);

    TicketResponse updateTicketStatus(UUID id, String status);

    void deleteTicket(UUID id);
}