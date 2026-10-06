package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.property.admin.service.AdminFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/finance")
@RequiredArgsConstructor
public class AdminFinanceController {

    private final AdminFinanceService service;

    @GetMapping("/summary")
    public ApiResponse<PaymentSummaryDto> summary() {
        return ApiResponse.ok(service.getSummary());
    }
}