package com.apex.module.lease.service;

import com.apex.module.lease.enums.LeaseStatus;
import com.apex.module.lease.repository.LeaseRepository;
import com.platform.common.dtos.admin.LeaseStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseAdminService {

    private final LeaseRepository leaseRepository;

    @Transactional(readOnly = true)
    public LeaseStatsDto stats() {
        LocalDateTime firstOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        long active = safe(() -> leaseRepository.countByStatus(LeaseStatus.ACTIVE));
        long created = safe(() -> leaseRepository.countByCreatedAtAfter(firstOfMonth));
        long terminated = safe(() -> leaseRepository
                .countByStatusAndUpdatedAtAfter(LeaseStatus.TERMINATED, firstOfMonth));
        long pending = safe(() -> leaseRepository.countByStatus(LeaseStatus.DRAFT));

        LeaseStatsDto dto = new LeaseStatsDto(active, created, terminated, pending);
        log.debug("Lease stats: {}", dto);
        return dto;
    }

    private long safe(java.util.function.LongSupplier s) {
        try { return s.getAsLong(); }
        catch (Exception e) {
            log.warn("Lease count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}
