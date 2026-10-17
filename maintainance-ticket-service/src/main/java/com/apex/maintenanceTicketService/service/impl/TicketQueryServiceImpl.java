package com.apex.maintenanceTicketService.service.impl;

import com.apex.maintenanceTicketService.dto.response.TicketResponse;
import com.apex.maintenanceTicketService.exception.ResourceNotFoundException;
import com.apex.maintenanceTicketService.mapper.TicketMapper;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import com.apex.maintenanceTicketService.repository.TicketRepository;
import com.apex.maintenanceTicketService.service.TicketQueryService;
import com.platform.common.tenant.TenantContext;
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
        UUID orgId = TenantContext.require();

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return ticketRepository.findByOrgId(orgId).stream()
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
            return ticketRepository.findByOrgIdAndLandlordId(orgId, callerId).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return ticketRepository.findByOrgIdAndTenantId(orgId, callerId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID id) {
        UUID orgId = TenantContext.require();
        return ticketRepository.findByIdAndOrgId(id, orgId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUnit(UUID unitId) {
        UUID orgId = TenantContext.require();
        return ticketRepository.findByOrgIdAndUnitId(orgId, unitId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTenant(UUID tenantId) {
        UUID orgId = TenantContext.require();
        return ticketRepository.findByOrgIdAndTenantId(orgId, tenantId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByLandlord(UUID landlordId) {
        UUID orgId = TenantContext.require();
        return ticketRepository.findByOrgIdAndLandlordId(orgId, landlordId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        UUID orgId = TenantContext.require();
        try {
            TicketStatus enumStatus = TicketStatus.valueOf(status.toUpperCase());
            return ticketRepository.findByOrgIdAndStatus(orgId, enumStatus).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}