package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.platform.common.dtos.PageResponse;
import com.property.admin.dto.AuditLogDto;
import com.property.admin.service.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditService service;

    @GetMapping
    public ApiResponse<PageResponse<AuditLogDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action) {
        return ApiResponse.ok(service.list(page, size, actor, action));
    }
}