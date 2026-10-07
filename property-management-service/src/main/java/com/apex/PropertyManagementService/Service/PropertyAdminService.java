package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.repository.UnitRepository;
import com.platform.common.dtos.admin.PropertyStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyAdminService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public PropertyStatsDto stats() {
        long totalProperties = safe(propertyRepository::count);
        long totalUnits = safe(unitRepository::count);
        long occupied = safe(() -> unitRepository.countByStatus(UnitStatus.OCCUPIED));
        long vacant = safe(() -> unitRepository.countByStatus(UnitStatus.AVAILABLE));

        double occupancyRate = totalUnits == 0
                ? 0.0
                : (double) occupied / totalUnits;

        PropertyStatsDto dto = new PropertyStatsDto(
                totalProperties, totalUnits, occupied, vacant, occupancyRate);
        log.debug("Property stats: {}", dto);
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
