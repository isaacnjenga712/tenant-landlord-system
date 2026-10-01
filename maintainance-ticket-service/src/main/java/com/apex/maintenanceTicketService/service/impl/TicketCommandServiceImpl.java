package com.apex.maintenanceTicketService.service.impl;

import com.apex.maintenanceTicketService.dto.request.TicketCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TicketUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TicketResponse;
import com.apex.maintenanceTicketService.exception.ResourceNotFoundException;
import com.apex.maintenanceTicketService.mapper.TicketMapper;
import com.apex.maintenanceTicketService.model.Ticket;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import com.apex.maintenanceTicketService.producer.TicketEventPublisher;
import com.apex.maintenanceTicketService.repository.TicketRepository;
import com.apex.maintenanceTicketService.service.TicketCommandService;
import com.platform.common.events.maintenance.TicketCreatedEvent;
import com.platform.common.events.maintenance.TicketResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCommandServiceImpl implements TicketCommandService {

    private final TicketRepository ticketRepository;
    private final TicketMapper mapper;
    private final TicketEventPublisher eventPublisher;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request,
                                       String userIdHeader,
                                       String userRole,
                                       String tenantHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }

        UUID callerId;
        try {
            callerId = UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("X-User-Id is not a valid UUID: " + userIdHeader);
        }

        UUID tenantId;
        UUID landlordId;

        if ("LANDLORD".equalsIgnoreCase(userRole)) {
            landlordId = callerId;
            tenantId = request.getTenantId();
            if (tenantId == null) {
                throw new IllegalArgumentException(
                        "tenantId is required when a landlord creates a ticket");
            }
        } else {
            // Default: TENANT creating their own ticket
            tenantId = callerId;
            landlordId = request.getLandlordId();
            if (landlordId == null) {
                throw new IllegalArgumentException(
                        "landlordId is required when a tenant creates a ticket");
            }
        }

        Ticket ticket = mapper.toEntity(request, tenantId, landlordId);
        Ticket saved = ticketRepository.save(ticket);

        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(saved.getId());
        event.setUnitId(saved.getUnitId());
        event.setTenantId(saved.getTenantId());
        event.setLandlordId(saved.getLandlordId());   // ← added
        event.setTitle(saved.getTitle());
        event.setPriority(saved.getPriority().name());
        event.setCorrelationId(UUID.randomUUID());
        event.setEventType("maintenance.ticket.created");
        eventPublisher.publishTicketCreated(event, saved.getTenantId());
        log.info("Ticket created: {} by {} tenantId={} landlordId={}",
                saved.getId(), userRole, tenantId, landlordId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(UUID id, TicketUpdateRequest request) {
        Ticket ticket = findOrThrow(id);
        mapper.updateEntity(ticket, request);
        Ticket updated = ticketRepository.save(ticket);
        log.info("Ticket updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(UUID id, String status) {
        Ticket ticket = findOrThrow(id);
        try {
            ticket.setStatus(TicketStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        Ticket updated = ticketRepository.save(ticket);

        if (updated.getStatus() == TicketStatus.RESOLVED
                || updated.getStatus() == TicketStatus.CLOSED) {
            TicketResolvedEvent event = new TicketResolvedEvent();
            event.setTicketId(updated.getId());
            event.setUnitId(updated.getUnitId());
            event.setTenantId(updated.getTenantId());
            event.setLandlordId(updated.getLandlordId());   // ← added
            event.setTitle(updated.getTitle());
            event.setCorrelationId(UUID.randomUUID());
            event.setEventType("maintenance.ticket.resolved");
            eventPublisher.publishTicketResolved(event, updated.getTenantId());
        }

        log.info("Ticket status updated: {} → {}", updated.getId(), status);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = findOrThrow(id);
        ticketRepository.delete(ticket);
        log.info("Ticket deleted: {}", id);
    }

    private Ticket findOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }
}