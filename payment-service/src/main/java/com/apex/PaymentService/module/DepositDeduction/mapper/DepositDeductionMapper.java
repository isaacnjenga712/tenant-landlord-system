package com.apex.PaymentService.module.DepositDeduction.mapper;

import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionCreateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionUpdateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.response.DepositDeductionResponseDto;
import com.apex.PaymentService.module.DepositDeduction.entity.DepositDeduction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepositDeductionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DepositDeduction toEntity(DepositDeductionCreateDto dto);

    DepositDeductionResponseDto toResponseDto(DepositDeduction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "depositId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DepositDeductionUpdateDto dto, @MappingTarget DepositDeduction entity);
}
