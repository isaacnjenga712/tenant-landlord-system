package com.apex.auth.controller;

import com.apex.auth.service.UserAdminService;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import com.platform.common.dtos.admin.UpdateRoleRequest;
import com.platform.common.dtos.admin.UpdateStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal endpoints — reachable ONLY from other services via Eureka.
 * The gateway has no route for /api/v1/internal/**, so these are
 * unreachable from the public internet.
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserAdminService service;

    @GetMapping
    public PageResponse<AdminUserDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return service.list(page, size, role, status, search);
    }

    @GetMapping("/count")
    public long count(@RequestParam(required = false) String role) {
        return service.count(role);
    }

    @GetMapping("/{id}")
    public AdminUserDto get(@PathVariable("id") UUID publicId) {
        return service.get(publicId);
    }

    @PatchMapping("/{id}/role")
    public void updateRole(@PathVariable("id") UUID publicId,
                           @Valid @RequestBody UpdateRoleRequest req) {
        service.updateRole(publicId, req.role());
    }

    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable("id") UUID publicId,
                             @Valid @RequestBody UpdateStatusRequest req) {
        service.updateStatus(publicId, req.active());
    }

    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable("id") UUID publicId) {
        service.triggerPasswordReset(publicId);
    }
}
