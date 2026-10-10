package com.apex.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/landlord")
public class LandlordController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('LANDLORD')")
    public String landlordDashboard() {
        return "Landlord Dashboard - Only accessible by LANDLORD";
    }
}
