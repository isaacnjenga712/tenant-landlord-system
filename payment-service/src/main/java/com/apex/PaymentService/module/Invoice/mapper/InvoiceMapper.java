package com.apex.PaymentService.module.Invoice.mapper;

import com.apex.PaymentService.module.Invoice.dto.request.InvoiceCreateDto;
import com.apex.PaymentService.module.Invoice.dto.request.InvoiceUpdateDto;
import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
import com.apex.PaymentService.module.Invoice.entity.Invoice;
import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paidAmount", source = "paidAmount", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "lateFeeApplied", constant = "false")
    @Mapping(target = "status", expression = "java(deriveInitialStatus(dto))")
    Invoice toEntity(InvoiceCreateDto dto);

    InvoiceResponseDto toResponseDto(Invoice invoice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leaseId", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "periodStart", ignore = true)
    @Mapping(target = "periodEnd", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lateFeeApplied", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(InvoiceUpdateDto dto, @MappingTarget Invoice invoice);

    default InvoiceStatus deriveInitialStatus(InvoiceCreateDto dto) {
        LocalDate now = LocalDate.now();
        int grace = dto.getGracePeriodDays() != null ? dto.getGracePeriodDays() : 3;
        if (now.isAfter(dto.getDueDate().plusDays(grace))) return InvoiceStatus.overdue;
        return InvoiceStatus.pending;
    }
}
