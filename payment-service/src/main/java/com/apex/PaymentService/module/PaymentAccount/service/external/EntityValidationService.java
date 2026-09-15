package com.apex.PaymentService.module.PaymentAccount.service.external;

import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class EntityValidationService {

    public boolean exists(EntityType entityType, UUID entityId) {
        // TODO: call Tenant/Landlord/Property microservices or repositories
        log.debug("Validating existence of {} with ID: {}", entityType, entityId);
        return true; // mock – replace with real logic
    }
}