package com.apex.PaymentService.module.landlord.service;

import com.apex.PaymentService.module.landlord.dto.request.LandlordCreateDto;
import com.apex.PaymentService.module.landlord.dto.request.LandlordUpdateDto;
import com.apex.PaymentService.module.landlord.dto.response.LandlordResponseDto;
import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LandlordService {
    LandlordResponseDto createLandlord(LandlordCreateDto dto);
    LandlordResponseDto getLandlord(UUID id);
    LandlordResponseDto getLandlordByEmail(String email);
    Page<LandlordResponseDto> listLandlords(LandlordStatus status, Pageable pageable);
    LandlordResponseDto updateLandlord(UUID id, LandlordUpdateDto dto);
    LandlordResponseDto updateFull(UUID id, LandlordCreateDto dto); // for PUT
    void deleteLandlord(UUID id);
    boolean exists(UUID id);
}
