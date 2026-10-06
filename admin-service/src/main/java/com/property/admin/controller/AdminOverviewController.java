package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.property.admin.dto.OverviewDto;
import com.property.admin.service.AdminOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/overview")
@RequiredArgsConstructor
public class AdminOverviewController {

    private final AdminOverviewService service;

    @GetMapping
    public ApiResponse<OverviewDto> overview() {
        return ApiResponse.ok(service.getOverview());
    }
}