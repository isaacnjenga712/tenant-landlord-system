package com.apex.PropertyManagementService.controllers;

import com.apex.PropertyManagementService.Service.PropertyAdminService;
import com.platform.common.dtos.admin.PropertyStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoints — reachable ONLY from other services via Eureka.
 * The gateway has no route for /api/v1/internal/**, so these are
 * unreachable from the public internet.
 */
@RestController
@RequestMapping("/api/v1/internal/properties")
@RequiredArgsConstructor
public class InternalPropertyController {

    private final PropertyAdminService service;

    @GetMapping("/stats")
    public PropertyStatsDto stats() {
        return service.stats();
    }
}
