package com.apex.PaymentService.module.landlord.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.landlord.dto.request.LandlordCreateDto;
import com.apex.PaymentService.module.landlord.dto.request.LandlordUpdateDto;
import com.apex.PaymentService.module.landlord.dto.response.LandlordResponseDto;
import com.apex.PaymentService.module.landlord.entity.Landlord;
import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import com.apex.PaymentService.module.landlord.mapper.LandlordMapper;
import com.apex.PaymentService.module.landlord.repository.LandlordRepository;
import com.apex.PaymentService.module.landlord.service.LandlordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandlordServiceImpl implements LandlordService {

    private final LandlordRepository repository;
    private final LandlordMapper mapper;

    @Override
    @Transactional
    public LandlordResponseDto createLandlord(LandlordCreateDto dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already exists: " + dto.getEmail());
        }
        Landlord landlord = mapper.toEntity(dto);
        if (landlord.getStatus() == null) landlord.setStatus(LandlordStatus.active);
        Landlord saved = repository.save(landlord);
        log.info("Created landlord: {}", saved.getEmail());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LandlordResponseDto getLandlord(UUID id) {
        Landlord landlord = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found"));
        return mapper.toResponseDto(landlord);
    }

    @Override
    @Transactional(readOnly = true)
    public LandlordResponseDto getLandlordByEmail(String email) {
        Landlord landlord = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found with email: " + email));
        return mapper.toResponseDto(landlord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandlordResponseDto> listLandlords(LandlordStatus status, Pageable pageable) {
        if (status != null) {
            return repository.findByStatus(status, pageable).map(mapper::toResponseDto);
        }
        return repository.findAll(pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public LandlordResponseDto updateLandlord(UUID id, LandlordUpdateDto dto) {
        Landlord landlord = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found"));
        if (dto.getEmail() != null && !dto.getEmail().equals(landlord.getEmail())) {
            if (repository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BusinessException("Email already in use: " + dto.getEmail());
            }
        }
        mapper.updateEntity(dto, landlord);
        Landlord updated = repository.save(landlord);
        log.info("Updated landlord: {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public LandlordResponseDto updateFull(UUID id, LandlordCreateDto dto) {
        Landlord landlord = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found"));
        // Check email uniqueness if changed
        if (!dto.getEmail().equals(landlord.getEmail()) &&
                repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already in use: " + dto.getEmail());
        }
        // Full replacement
        landlord.setCompanyName(dto.getCompanyName());
        landlord.setContactPerson(dto.getContactPerson());
        landlord.setEmail(dto.getEmail());
        landlord.setPhone(dto.getPhone());
        landlord.setStatus(dto.getStatus() != null ? dto.getStatus() : LandlordStatus.active);
        Landlord updated = repository.save(landlord);
        log.info("Full updated landlord: {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteLandlord(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Landlord not found");
        }
        repository.deleteById(id);
        log.info("Deleted landlord: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }
}