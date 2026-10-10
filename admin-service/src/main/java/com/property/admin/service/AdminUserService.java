package com.property.admin.service;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import com.platform.common.dtos.admin.UpdateRoleRequest;
import com.platform.common.dtos.admin.UpdateStatusRequest;
import com.property.admin.client.AuthClient;
import com.property.admin.exception.ResourceNotFoundException;
import com.property.admin.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final AuthClient authClient;

    public PageResponse<AdminUserDto> list(int page, int size,
                                            String role, String status, String search) {
        return authClient.listUsers(page, size, role, status, search);
    }

    public AdminUserDto get(String id) {
        AdminUserDto user = authClient.getUser(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        return user;
    }

    public void updateRole(String id, UpdateRoleRequest req) {
        try {
            authClient.updateRole(id, req);
            log.info("Role updated for user {} → {}", id, req.role());
        } catch (Exception e) {
            throw new UpstreamServiceException("auth-service", e);
        }
    }

    public void updateStatus(String id, UpdateStatusRequest req) {
        try {
            authClient.updateStatus(id, req);
            log.info("Status updated for user {} → active={}", id, req.active());
        } catch (Exception e) {
            throw new UpstreamServiceException("auth-service", e);
        }
    }

    public void triggerPasswordReset(String id) {
        try {
            authClient.triggerPasswordReset(id);
            log.info("Password reset triggered for user {}", id);
        } catch (Exception e) {
            throw new UpstreamServiceException("auth-service", e);
        }
    }
}