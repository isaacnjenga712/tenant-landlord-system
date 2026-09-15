package com.apex.PaymentService.module.Tenant.mapper;

import com.apex.PaymentService.module.Tenant.dto.request.TenantCreateDto;
import com.apex.PaymentService.module.Tenant.dto.request.TenantUpdateDto;
import com.apex.PaymentService.module.Tenant.dto.response.TenantResponseDto;
import com.apex.PaymentService.module.Tenant.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tenant toEntity(TenantCreateDto dto);

    TenantResponseDto toResponseDto(Tenant tenant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TenantUpdateDto dto, @MappingTarget Tenant tenant);
}
