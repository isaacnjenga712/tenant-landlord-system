package com.apex.PaymentService.module.landlord.repository;

import com.apex.PaymentService.module.landlord.entity.Landlord;
import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LandlordRepository extends JpaRepository<Landlord, UUID> {
    Optional<Landlord> findByEmail(String email);
    Page<Landlord> findByStatus(LandlordStatus status, Pageable pageable);
}
