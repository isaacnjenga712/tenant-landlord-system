package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.model.enums.TicketStatus;
import com.apex.maintenanceTicketService.repository.TicketRepository;
import com.platform.common.dtos.admin.MaintenanceStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceAdminService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public MaintenanceStatsDto stats() {
        LocalDateTime firstOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        long open = safe(() -> ticketRepository.countByStatus(TicketStatus.OPEN));
        long inProgress = safe(() -> ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        long resolved = safe(() -> ticketRepository
                .countByStatusAndUpdatedAtAfter(TicketStatus.RESOLVED, firstOfMonth));
        long slaBreach = 0L;  // no SLA field on Ticket entity yet

        MaintenanceStatsDto dto = new MaintenanceStatsDto(open, inProgress, resolved, slaBreach);
        log.debug("Maintenance stats: {}", dto);
        return dto;
    }

    private long safe(java.util.function.LongSupplier s) {
        try { return s.getAsLong(); }
        catch (Exception e) {
            log.warn("Ticket count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}
