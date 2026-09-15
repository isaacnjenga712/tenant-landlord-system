package com.apex.PaymentService.module.SecurityDeposit.mapper;

import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositCreateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositUpdateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.response.SecurityDepositResponseDto;
import com.apex.PaymentService.module.SecurityDeposit.entity.SecurityDeposit;
import com.apex.PaymentService.module.SecurityDeposit.enums.DepositStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SecurityDepositMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currentBalance", source = "totalDeposit")
    @Mapping(target = "deductionTotal", constant = "0")
    @Mapping(target = "interestAccrued", constant = "0")
    @Mapping(target = "returnedDate", ignore = true)
    @Mapping(target = "returnAmount", ignore = true)
    SecurityDeposit toEntity(SecurityDepositCreateDto dto);

    @Mapping(target = "status", expression = "java(deposit.getDerivedStatus())")
    SecurityDepositResponseDto toResponseDto(SecurityDeposit deposit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leaseId", ignore = true)
    @Mapping(target = "totalDeposit", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "deductionTotal", ignore = true)
    @Mapping(target = "interestAccrued", ignore = true)
    @Mapping(target = "returnedDate", ignore = true)
    @Mapping(target = "returnAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(SecurityDepositUpdateDto dto, @MappingTarget SecurityDeposit deposit);
}
