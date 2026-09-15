package com.apex.PaymentService.module.InvoiceLineItem.mapper;

import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemCreateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemUpdateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.response.InvoiceLineItemResponseDto;
import com.apex.PaymentService.module.InvoiceLineItem.entity.InvoiceLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InvoiceLineItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "total", ignore = true) // will be calculated in service
    InvoiceLineItem toEntity(InvoiceLineItemCreateDto dto);

    InvoiceLineItemResponseDto toResponseDto(InvoiceLineItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "total", ignore = true) // recalc in service
    void updateEntity(InvoiceLineItemUpdateDto dto, @MappingTarget InvoiceLineItem entity);
}
