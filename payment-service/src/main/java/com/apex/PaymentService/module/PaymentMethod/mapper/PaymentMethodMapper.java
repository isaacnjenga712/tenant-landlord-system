package com.apex.PaymentService.module.PaymentMethod.mapper;

import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodCreateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodUpdateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.response.PaymentMethodResponseDto;
import com.apex.PaymentService.module.PaymentMethod.entity.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDefault", source = "isDefault", defaultExpression = "java(false)")
    @Mapping(target = "isActive", constant = "true")
    PaymentMethod toEntity(PaymentMethodCreateDto dto);

    PaymentMethodResponseDto toResponseDto(PaymentMethod entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "gatewayCustomerId", ignore = true)
    @Mapping(target = "gatewayPaymentMethodId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PaymentMethodUpdateDto dto, @MappingTarget PaymentMethod entity);
}
