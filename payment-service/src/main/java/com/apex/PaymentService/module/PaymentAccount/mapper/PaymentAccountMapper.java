package com.apex.PaymentService.module.PaymentAccount.mapper;

import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountCreateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountUpdateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.response.PaymentAccountResponseDto;
import com.apex.PaymentService.module.PaymentAccount.entity.PaymentAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentAccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    PaymentAccount toEntity(PaymentAccountCreateDto dto);

    PaymentAccountResponseDto toResponseDto(PaymentAccount account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "trustBalance", ignore = true)
    @Mapping(target = "operationalBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(PaymentAccountUpdateDto dto, @MappingTarget PaymentAccount account);
}
