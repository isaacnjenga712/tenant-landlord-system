package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.response.TicketResponse;

import java.util.List;
import java.util.UUID;

public interface TicketQueryService {
    List<TicketResponse> getAllTickets();
    TicketResponse getTicketById(UUID id);
    List<TicketResponse> getTicketsByUnit(UUID unitId);
    List<TicketResponse> getTicketsByTenant(UUID tenantId);
    List<TicketResponse> getTicketsByStatus(String status);
}
