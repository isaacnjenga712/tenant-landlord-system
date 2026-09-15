package com.apex.PropertyManagementService.mapper;

import com.apex.PropertyManagementService.DTOs.request.LandlordCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.LandlordUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.LandlordResponse;
import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;               // ✅ ADD THIS IMPORT
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LandlordMapper {

    Landlord toEntity(LandlordCreateRequest request);

    @Mapping(source = "properties", target = "propertyIds", qualifiedByName = "mapPropertyIds")
    LandlordResponse toResponse(Landlord landlord);

    void updateEntity(@MappingTarget Landlord landlord, LandlordUpdateRequest request);

    List<LandlordResponse> toResponseList(List<Landlord> landlords);

    @Named("mapPropertyIds")   // ✅ ADD THIS
    default List<UUID> mapPropertyIds(List<Property> properties) {
        if (properties == null) return List.of();
        return properties.stream().map(Property::getId).collect(Collectors.toList());
    }
}
