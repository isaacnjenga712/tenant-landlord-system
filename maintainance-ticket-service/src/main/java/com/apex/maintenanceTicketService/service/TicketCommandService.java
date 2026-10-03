package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.request.TicketCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TicketUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TicketResponse;

import java.util.UUID;

public interface TicketCommandService {

    /**
     * Create a ticket. The service resolves tenantId and landlordId from the
     * X-User-Id (publicId from JWT) and X-User-Role headers. tenantHeader is
     * optional and may be non-UUID ("default").
     */
    TicketResponse createTicket(TicketCreateRequest request,
                                String userIdHeader,
                                String userRole,
                                String tenantHeader);

    TicketResponse updateTicket(UUID id, TicketUpdateRequest request);

    TicketResponse updateTicketStatus(UUID id, String status);

    void deleteTicket(UUID id);
}