package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.response.TicketResponse;

import java.util.List;
import java.util.UUID;

public interface TicketQueryService {

    /**
     * List tickets, filtered by the caller's role:
     *  - TENANT  → only tickets where tenantId == caller's publicId
     *  - LANDLORD → only tickets where landlordId == caller's publicId
     *  - ADMIN   → all tickets
     */
    List<TicketResponse> getAllTickets(String userIdHeader, String userRole);

    TicketResponse getTicketById(UUID id);

    List<TicketResponse> getTicketsByUnit(UUID unitId);

    List<TicketResponse> getTicketsByTenant(UUID tenantId);

    List<TicketResponse> getTicketsByLandlord(UUID landlordId);

    List<TicketResponse> getTicketsByStatus(String status);
}