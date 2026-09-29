package com.apex.maintenanceTicketService.service.impl;

import com.apex.maintenanceTicketService.dto.response.TicketResponse;
import com.apex.maintenanceTicketService.exception.ResourceNotFoundException;
import com.apex.maintenanceTicketService.mapper.TicketMapper;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import com.apex.maintenanceTicketService.repository.TicketRepository;
import com.apex.maintenanceTicketService.service.TicketQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketQueryServiceImpl implements TicketQueryService {

    private final TicketRepository ticketRepository;
    private final TicketMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(String userIdHeader, String userRole) {
        // Admin sees all
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return ticketRepository.findAll().stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return List.of();
        }

        UUID callerId;
        try {
            callerId = UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Non-UUID X-User-Id: {}", userIdHeader);
            return List.of();
        }

        if ("LANDLORD".equalsIgnoreCase(userRole)) {
            return ticketRepository.findByLandlordId(callerId).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        // TENANT (default)
        return ticketRepository.findByTenantId(callerId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUnit(UUID unitId) {
        return ticketRepository.findByUnitId(unitId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTenant(UUID tenantId) {
        return ticketRepository.findByTenantId(tenantId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByLandlord(UUID landlordId) {
        return ticketRepository.findByLandlordId(landlordId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        try {
            TicketStatus enumStatus = TicketStatus.valueOf(status.toUpperCase());
            return ticketRepository.findByStatus(enumStatus).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}