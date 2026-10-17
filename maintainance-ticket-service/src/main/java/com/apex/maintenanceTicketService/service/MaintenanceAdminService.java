package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import com.apex.maintenanceTicketService.repository.TicketRepository;
import com.platform.common.dtos.admin.MaintenanceStatsDto;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.LongSupplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceAdminService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public MaintenanceStatsDto stats() {
        UUID orgId = TenantContext.require();

        LocalDateTime firstOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        long open       = safe(() -> ticketRepository.countByOrgIdAndStatus(orgId, TicketStatus.OPEN));
        long inProgress = safe(() -> ticketRepository.countByOrgIdAndStatus(orgId, TicketStatus.IN_PROGRESS));
        long resolved   = safe(() -> ticketRepository.countByOrgIdAndStatusAndUpdatedAtAfter(orgId, TicketStatus.RESOLVED, firstOfMonth));

        // SLA breach calculation is TODO — return 0 until we add a due_at column and business rule.
        long slaBreach  = 0L;

        MaintenanceStatsDto dto = new MaintenanceStatsDto(open, inProgress, resolved, slaBreach);
        log.debug("Maintenance stats for org {}: {}", orgId, dto);
        return dto;
    }

    private long safe(LongSupplier s) {
        try { return s.getAsLong(); }
        catch (Exception e) {
            log.warn("Maintenance count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}