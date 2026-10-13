package com.apex.module.lease.service;

import com.apex.module.lease.enums.LeaseStatus;
import com.apex.module.lease.repository.LeaseRepository;
import com.platform.common.dtos.admin.LeaseStatsDto;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseAdminService {

    private final LeaseRepository leaseRepository;

    @Transactional(readOnly = true)
    public LeaseStatsDto stats() {
        UUID orgId = TenantContext.require();   // fail fast — outside the safe() wrappers

        LocalDateTime firstOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        long active = safe(() -> leaseRepository.countByOrgIdAndStatus(orgId, LeaseStatus.ACTIVE));
        long created = safe(() -> leaseRepository.countByOrgIdAndCreatedAtAfter(orgId, firstOfMonth));
        long terminated = safe(() -> leaseRepository
                .countByOrgIdAndStatusAndUpdatedAtAfter(orgId, LeaseStatus.TERMINATED, firstOfMonth));
        long pending = safe(() -> leaseRepository.countByOrgIdAndStatus(orgId, LeaseStatus.DRAFT));

        LeaseStatsDto dto = new LeaseStatsDto(active, created, terminated, pending);
        log.debug("Lease stats for org {}: {}", orgId, dto);
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