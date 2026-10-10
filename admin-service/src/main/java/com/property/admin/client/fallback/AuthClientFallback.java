package com.property.admin.client.fallback;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import com.platform.common.dtos.admin.UpdateRoleRequest;
import com.platform.common.dtos.admin.UpdateStatusRequest;
import com.property.admin.client.AuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthClientFallback implements AuthClient {

    private static final String MSG = "auth-service unavailable";

    @Override
    public PageResponse<AdminUserDto> listUsers(int page, int size,
                                                 String role, String status, String search) {
        log.warn("{} — listUsers fallback", MSG);
        return PageResponse.empty(page, size);
    }

    @Override
    public AdminUserDto getUser(String id) {
        log.warn("{} — getUser fallback for {}", MSG, id);
        return null;
    }

    @Override
    public void updateRole(String id, UpdateRoleRequest request) {
        log.warn("{} — updateRole fallback for {}", MSG, id);
        throw new RuntimeException(MSG);
    }

    @Override
    public void updateStatus(String id, UpdateStatusRequest request) {
        log.warn("{} — updateStatus fallback for {}", MSG, id);
        throw new RuntimeException(MSG);
    }

    @Override
    public long countUsers(String role) {
        return 0L;
    }

    @Override
    public void triggerPasswordReset(String id) {
        log.warn("{} — resetPassword fallback for {}", MSG, id);
        throw new RuntimeException(MSG);
    }
}