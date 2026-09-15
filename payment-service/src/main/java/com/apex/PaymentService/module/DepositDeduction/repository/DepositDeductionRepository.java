package com.apex.PaymentService.module.DepositDeduction.repository;

import com.apex.PaymentService.module.DepositDeduction.entity.DepositDeduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DepositDeductionRepository extends JpaRepository<DepositDeduction, UUID> {

    List<DepositDeduction> findByDepositId(UUID depositId);

    Page<DepositDeduction> findByDepositId(UUID depositId, Pageable pageable);

    void deleteByDepositId(UUID depositId);
}