package com.apex.PropertyManagementService.consumer;

import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.Unit;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.repository.UnitRepository;
import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.lease.LeaseTerminatedEvent;
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
public class LeaseTerminatedConsumer {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    @KafkaListener(topics = KafkaTopics.LEASE_TERMINATED, groupId = "property-group")
    public void handleLeaseTerminated(LeaseTerminatedEvent event, Acknowledgment ack) {
        log.info("Lease terminated for property: {}, tenant: {}, reason: {}",
                event.getPropertyId(), event.getTenantId(), event.getReason());

        try {
            UUID propertyId = event.getPropertyId();

            // Revert property status to AVAILABLE
            propertyRepository.findByPropertyId(propertyId)
                    .ifPresentOrElse(
                            property -> {
                                property.setStatus(PropertyStatus.AVAILABLE);
                                propertyRepository.save(property);
                                log.info("Property {} marked as AVAILABLE", propertyId);
                            },
                            () -> log.warn("Property not found: {}", propertyId)
                    );

            // ✅ Handle list of units for the property
            List<Unit> units = unitRepository.findByPropertyId(propertyId);
            if (!units.isEmpty()) {
                Unit unit = units.get(0);
                unit.setStatus(UnitStatus.AVAILABLE);
                unit.setCurrentTenantId(null);
                unitRepository.save(unit);
                log.info("Unit {} (property {}) marked as AVAILABLE", unit.getUnitNumber(), propertyId);
            } else {
                log.debug("No units found for propertyId: {}", propertyId);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing LeaseTerminatedEvent: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}