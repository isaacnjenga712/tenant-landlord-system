package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.property.admin.dto.ServiceHealthDto;
import com.property.admin.service.AdminSystemHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/system")
@RequiredArgsConstructor
public class AdminSystemHealthController {

    private final AdminSystemHealthService service;

    @GetMapping("/services")
    public ApiResponse<List<ServiceHealthDto>> services() {
        return ApiResponse.ok(service.getServices());
    }
}