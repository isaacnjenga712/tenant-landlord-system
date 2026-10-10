package com.property.admin.service;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.property.admin.dto.ServiceHealthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSystemHealthService {

    private final EurekaClient eurekaClient;

    private static final List<String> WATCHED_SERVICES = List.of(
            "GATEWAY-SERVICE",
            "AUTH-SERVICE",
            "PROPERTY-MANAGEMENT-SERVICE",
            "LEASE-SERVICE",
            "PAYMENT-SERVICE",
            "MPESA-SERVICE",
            "MAINTENANCE-TICKET-SERVICE",
            "NOTIFICATION-ENGINE",
            "ADMIN-SERVICE"
    );

    public List<ServiceHealthDto> getServices() {
        List<ServiceHealthDto> result = new ArrayList<>();
        for (String name : WATCHED_SERVICES) {
            Application app = eurekaClient.getApplication(name);
            if (app == null || app.getInstances().isEmpty()) {
                result.add(new ServiceHealthDto(name, "DOWN", null, null, null));
                continue;
            }
            var instance = app.getInstances().get(0);
            result.add(new ServiceHealthDto(
                    name,
                    instance.getStatus() != null ? instance.getStatus().name() : "UNKNOWN",
                    instance.getMetadata().getOrDefault("version", "n/a"),
                    instance.getInstanceId(),
                    Instant.now()
            ));
        }
        return result;
    }
}
