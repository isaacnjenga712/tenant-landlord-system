package com.apex.PaymentService.module.AuditLog.controller;

import com.apex.PaymentService.module.AuditLog.dto.request.AuditLogFilterDto;
import com.apex.PaymentService.module.AuditLog.dto.response.AuditLogResponseDto;
import com.apex.PaymentService.module.AuditLog.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<AuditLogResponseDto>> getByAccount(
            @PathVariable UUID accountId,
            @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAuditLogs(accountId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuditLogResponseDto>> search(
            AuditLogFilterDto filter,
            @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAuditLogsWithFilters(filter, pageable));
    }
}
