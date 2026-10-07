package com.apex.maintenanceTicketService.controller;

import com.apex.maintenanceTicketService.service.MaintenanceAdminService;
import com.platform.common.dtos.admin.MaintenanceStatsDto;
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
@RequestMapping("/api/v1/internal/tickets")
@RequiredArgsConstructor
public class InternalTicketController {

    private final MaintenanceAdminService service;

    @GetMapping("/stats")
    public MaintenanceStatsDto stats() {
        return service.stats();
    }
}
