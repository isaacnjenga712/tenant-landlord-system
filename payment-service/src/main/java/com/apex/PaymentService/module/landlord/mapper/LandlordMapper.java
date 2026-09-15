package com.apex.PaymentService.module.landlord.mapper;

import com.apex.PaymentService.module.landlord.dto.request.LandlordCreateDto;
import com.apex.PaymentService.module.landlord.dto.request.LandlordUpdateDto;
import com.apex.PaymentService.module.landlord.dto.response.LandlordResponseDto;
import com.apex.PaymentService.module.landlord.entity.Landlord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LandlordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Landlord toEntity(LandlordCreateDto dto);

    LandlordResponseDto toResponseDto(Landlord landlord);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LandlordUpdateDto dto, @MappingTarget Landlord landlord);
}
