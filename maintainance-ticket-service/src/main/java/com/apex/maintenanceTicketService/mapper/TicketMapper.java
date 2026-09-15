package com.apex.maintenanceTicketService.mapper;

import com.apex.maintenanceTicketService.dto.request.TicketCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TicketUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TicketResponse;
import com.apex.maintenanceTicketService.model.Ticket;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TicketMapper {

    public Ticket toEntity(TicketCreateRequest request, UUID tenantId, UUID landlordId) {
        return Ticket.builder()
                .unitId(request.getUnitId())
                .tenantId(tenantId)
                .landlordId(landlordId)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(TicketStatus.OPEN)
                .build();
    }

    public TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .unitId(ticket.getUnitId())
                .tenantId(ticket.getTenantId())
                .landlordId(ticket.getLandlordId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public void updateEntity(Ticket ticket, TicketUpdateRequest request) {
        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getStatus() != null) ticket.setStatus(request.getStatus());
    }
}