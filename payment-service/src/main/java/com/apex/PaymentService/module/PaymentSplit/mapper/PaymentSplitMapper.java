package com.apex.PaymentService.module.PaymentSplit.mapper;

import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitCreateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.request.PaymentSplitUpdateDto;
import com.apex.PaymentService.module.PaymentSplit.dto.response.PaymentSplitResponseDto;
import com.apex.PaymentService.module.PaymentSplit.entity.PaymentSplit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentSplitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentSplit toEntity(PaymentSplitCreateDto dto);

    PaymentSplitResponseDto toResponseDto(PaymentSplit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PaymentSplitUpdateDto dto, @MappingTarget PaymentSplit entity);
}
