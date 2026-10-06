package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import com.platform.common.dtos.admin.UpdateRoleRequest;
import com.platform.common.dtos.admin.UpdateStatusRequest;
import com.property.admin.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping
    public ApiResponse<PageResponse<AdminUserDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok(service.list(page, size, role, status, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserDto> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable String id,
                                        @Valid @RequestBody UpdateRoleRequest req) {
        service.updateRole(id, req);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String id,
                                          @Valid @RequestBody UpdateStatusRequest req) {
        service.updateStatus(id, req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable String id) {
        service.triggerPasswordReset(id);
        return ApiResponse.ok(null);
    }
}