package com.property.admin.client;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import com.platform.common.dtos.admin.UpdateRoleRequest;
import com.platform.common.dtos.admin.UpdateStatusRequest;
import com.property.admin.client.fallback.AuthClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "AUTH-SERVICE",
        path = "/api/v1/internal",
        fallback = AuthClientFallback.class
)
public interface AuthClient {

    @GetMapping("/users")
    PageResponse<AdminUserDto> listUsers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search);

    @GetMapping("/users/{id}")
    AdminUserDto getUser(@PathVariable("id") String id);

    @PatchMapping("/users/{id}/role")
    void updateRole(@PathVariable("id") String id,
                    @RequestBody UpdateRoleRequest request);

    @PatchMapping("/users/{id}/status")
    void updateStatus(@PathVariable("id") String id,
                      @RequestBody UpdateStatusRequest request);

    @GetMapping("/users/count")
    long countUsers(@RequestParam(value = "role", required = false) String role);

    @PostMapping("/users/{id}/reset-password")
    void triggerPasswordReset(@PathVariable("id") String id);
}