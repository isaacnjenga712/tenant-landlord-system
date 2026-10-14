package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.repository.UnitRepository;
import com.platform.common.dtos.admin.PropertyStatsDto;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyAdminService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public PropertyStatsDto stats() {
        UUID orgId = TenantContext.require();

        long totalProperties = safe(() -> propertyRepository.findByOrgId(orgId).size());
        long totalUnits = safe(() -> unitRepository.findByOrgId(orgId).size());
        long occupied = safe(() -> unitRepository.countByOrgIdAndStatus(orgId, UnitStatus.OCCUPIED));
        long vacant = safe(() -> unitRepository.countByOrgIdAndStatus(orgId, UnitStatus.AVAILABLE));

        double occupancyRate = totalUnits == 0
                ? 0.0
                : (double) occupied / totalUnits;

        PropertyStatsDto dto = new PropertyStatsDto(
                totalProperties, totalUnits, occupied, vacant, occupancyRate);
        log.debug("Property stats for org {}: {}", orgId, dto);
        return dto;
    }

    private long safe(java.util.function.LongSupplier s) {
        try { return s.getAsLong(); }
        catch (Exception e) {
            log.warn("Property count query failed: {}", e.getMessage());
            return 0L;
        }
    }
}