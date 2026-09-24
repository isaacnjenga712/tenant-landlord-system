package com.apex.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenant")
public class TenantController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TENANT')")
    public String tenantDashboard() {
        return "Tenant Dashboard - Only accessible by TENANT";
    }
}
