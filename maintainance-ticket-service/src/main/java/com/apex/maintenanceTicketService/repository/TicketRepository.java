package com.apex.maintenanceTicketService.repository;

import com.apex.maintenanceTicketService.model.Ticket;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    // ---- org-scoped lookups ----

    Optional<Ticket> findByIdAndOrgId(UUID id, UUID orgId);

    List<Ticket> findByOrgId(UUID orgId);

    List<Ticket> findByOrgIdAndUnitId(UUID orgId, UUID unitId);

    List<Ticket> findByOrgIdAndTenantId(UUID orgId, UUID tenantId);

    List<Ticket> findByOrgIdAndLandlordId(UUID orgId, UUID landlordId);

    List<Ticket> findByOrgIdAndStatus(UUID orgId, TicketStatus status);

    long countByOrgIdAndStatus(UUID orgId, TicketStatus status);

    long countByOrgIdAndStatusAndUpdatedAtAfter(UUID orgId, TicketStatus status, LocalDateTime after);
}