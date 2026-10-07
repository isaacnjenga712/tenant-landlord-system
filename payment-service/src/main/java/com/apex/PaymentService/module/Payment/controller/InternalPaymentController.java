package com.apex.PaymentService.module.Payment.controller;

import com.apex.PaymentService.module.Payment.service.PaymentAdminService;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminPaymentDto;
import com.platform.common.dtos.admin.PaymentSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoints — reachable ONLY from other services via Eureka.
 * The gateway has no route for /api/v1/internal/**, so these are
 * unreachable from the public internet.
 */
@RestController
@RequestMapping("/api/v1/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentAdminService service;

    @GetMapping("/summary")
    public PaymentSummaryDto summary() {
        return service.summary();
    }

    @GetMapping("/list")
    public PageResponse<AdminPaymentDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method) {
        return service.list(page, size, status, method);
    }
}