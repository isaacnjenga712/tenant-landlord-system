package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.DTOs.request.LandlordCreateRequest;
import com.apex.PropertyManagementService.DTOs.response.LandlordResponse;
import com.apex.PropertyManagementService.DTOs.request.LandlordUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface LandlordService {

    LandlordResponse createLandlord(LandlordCreateRequest request);

    List<LandlordResponse> getAllLandlords();

    LandlordResponse getLandlordById(UUID id);

    LandlordResponse getLandlordByEmail(String email);

    LandlordResponse updateLandlord(UUID id, LandlordUpdateRequest request);

    void deleteLandlord(UUID id);
}