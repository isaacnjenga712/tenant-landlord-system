package com.apex.module.lease.controller;

import com.apex.module.lease.service.LeaseAdminService;
import com.platform.common.dtos.admin.LeaseStatsDto;
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
@RequestMapping("/api/v1/internal/leases")
@RequiredArgsConstructor
public class InternalLeaseController {

    private final LeaseAdminService service;

    @GetMapping("/stats")
    public LeaseStatsDto stats() {
        return service.stats();
    }
}
