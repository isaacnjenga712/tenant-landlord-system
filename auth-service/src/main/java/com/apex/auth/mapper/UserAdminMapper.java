package com.apex.auth.mapper;

import com.apex.auth.entity.User;
import com.platform.common.dtos.admin.AdminUserDto;
import org.springframework.stereotype.Component;

@Component
public class UserAdminMapper {

    public AdminUserDto toDto(User u) {
        if (u == null) return null;
        return new AdminUserDto(
                u.getPublicId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole().name(),
                u.isEnabled(),
                null,   // phone — not on User entity yet
                u.getCreatedAt(),
                null    // lastLoginAt — not tracked yet
        );
    }
}
