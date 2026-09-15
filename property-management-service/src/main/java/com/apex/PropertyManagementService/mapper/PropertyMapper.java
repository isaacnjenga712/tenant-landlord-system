package com.apex.PropertyManagementService.mapper;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.entity.Property;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component   // ✅ Add this annotation
public class PropertyMapper {

    public Property toEntity(PropertyCreateRequest request) {
        if (request == null) return null;
        Property property = new Property();
        property.setPropertyId(request.getPropertyId());
        property.setLandlord(null); // set later in service
        property.setAddressLine1(request.getAddressLine1());
        property.setAddressLine2(request.getAddressLine2());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setZipCode(request.getZipCode());
        property.setCountry(request.getCountry());
        return property;
    }

    public PropertyResponse toResponse(Property property) {
        if (property == null) return null;
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyId(property.getPropertyId())
                .landlordId(property.getLandlord() != null ? property.getLandlord().getId() : null)
                .addressLine1(property.getAddressLine1())
                .addressLine2(property.getAddressLine2())
                .city(property.getCity())
                .state(property.getState())
                .zipCode(property.getZipCode())
                .country(property.getCountry())
                .status(property.getStatus() != null ? property.getStatus().name() : null)
                .unitIds(property.getUnits() != null ?
                        property.getUnits().stream().map(unit -> unit.getId()).collect(Collectors.toList()) :
                        List.of())
                .build();
    }

    public List<PropertyResponse> toResponseList(List<Property> properties) {
        if (properties == null) return List.of();
        return properties.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void updateEntity(Property property, PropertyUpdateRequest request) {
        if (request == null) return;
        if (request.getAddressLine1() != null) property.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) property.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) property.setCity(request.getCity());
        if (request.getState() != null) property.setState(request.getState());
        if (request.getZipCode() != null) property.setZipCode(request.getZipCode());
        if (request.getCountry() != null) property.setCountry(request.getCountry());
        // status is handled separately in service
    }
}