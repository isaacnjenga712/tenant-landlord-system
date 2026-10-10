package com.apex.maintenanceTicketService.repository;

import com.apex.maintenanceTicketService.model.Ticket;
import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByUnitId(UUID unitId);

    List<Ticket> findByTenantId(UUID tenantId);

    List<Ticket> findByLandlordId(UUID landlordId);

    List<Ticket> findByStatus(TicketStatus status);

    // ---------- KPI counts for admin dashboard ----------

    long countByStatus(TicketStatus status);

    long countByStatusAndUpdatedAtAfter(TicketStatus status, LocalDateTime after);
}
