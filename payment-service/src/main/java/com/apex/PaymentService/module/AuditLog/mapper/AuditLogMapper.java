package com.apex.PaymentService.module.AuditLog.mapper;

import com.apex.PaymentService.module.AuditLog.dto.response.AuditLogResponseDto;
import com.apex.PaymentService.module.AuditLog.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponseDto toResponseDto(AuditLog entity);
}