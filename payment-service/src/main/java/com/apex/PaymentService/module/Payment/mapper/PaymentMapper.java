package com.apex.PaymentService.module.Payment.mapper;

import com.apex.PaymentService.module.Payment.dto.request.PaymentCreateDto;
import com.apex.PaymentService.module.Payment.dto.request.PaymentUpdateDto;
import com.apex.PaymentService.module.Payment.dto.response.PaymentResponseDto;
import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "status", expression = "java(deriveInitialStatus())")
    @Mapping(target = "appliedToInvoice", constant = "false")
    Payment toEntity(PaymentCreateDto dto);

    PaymentResponseDto toResponseDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "appliedToInvoice", ignore = true)
    void updateEntity(PaymentUpdateDto dto, @MappingTarget Payment payment);

    default PaymentStatus deriveInitialStatus() {
        return PaymentStatus.initiated;
    }
}
