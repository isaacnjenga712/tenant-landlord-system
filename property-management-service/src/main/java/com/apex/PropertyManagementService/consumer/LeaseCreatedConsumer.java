package com.apex.PropertyManagementService.consumer;

import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.Unit;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.repository.UnitRepository;
import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.lease.LeaseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseCreatedConsumer {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    @KafkaListener(topics = KafkaTopics.LEASE_CREATED, groupId = "property-group")
    public void handleLeaseCreated(LeaseCreatedEvent event, Acknowledgment ack) {
        log.info("Lease created for property: {}, tenant: {}", event.getPropertyId(), event.getTenantId());

        try {
            UUID propertyId = event.getPropertyId();
            UUID tenantId = event.getTenantId();

            // Update property status
            propertyRepository.findByPropertyId(propertyId)
                    .ifPresentOrElse(
                            property -> {
                                property.setStatus(PropertyStatus.OCCUPIED);
                                propertyRepository.save(property);
                                log.info("Property {} marked as OCCUPIED", propertyId);
                            },
                            () -> log.warn("Property not found: {}", propertyId)
                    );

            // ✅ Handle list of units for the property
            List<Unit> units = unitRepository.findByPropertyId(propertyId);
            if (!units.isEmpty()) {
                // For simplicity, update the first unit (or you could update all units)
                Unit unit = units.get(0);
                unit.setStatus(UnitStatus.OCCUPIED);
                unit.setCurrentTenantId(tenantId.toString());
                unitRepository.save(unit);
                log.info("Unit {} (property {}) marked as OCCUPIED by tenant {}", unit.getUnitNumber(), propertyId, tenantId);
            } else {
                log.debug("No units found for propertyId: {}", propertyId);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing LeaseCreatedEvent: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}