package com.apex.PropertyManagementService.mapper;

import com.apex.PropertyManagementService.DTOs.request.UnitCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.UnitUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.UnitResponse;
import com.apex.PropertyManagementService.entity.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UnitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Unit toEntity(UnitCreateRequest request);

    @Mapping(source = "property.id", target = "propertyId")
    UnitResponse toResponse(Unit unit);

    void updateEntity(@MappingTarget Unit unit, UnitUpdateRequest request);

    List<UnitResponse> toResponseList(List<Unit> units);
}
