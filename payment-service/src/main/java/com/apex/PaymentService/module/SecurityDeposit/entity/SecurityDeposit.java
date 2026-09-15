package com.apex.PaymentService.module.SecurityDeposit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.apex.PaymentService.module.SecurityDeposit.enums.DepositStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "security_deposits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lease_id", nullable = false, unique = true)
    private UUID leaseId;

    @Column(name = "total_deposit", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalDeposit;

    @Column(name = "held_in_account_id")
    private UUID heldInAccountId;

    @Column(name = "current_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "interest_accrued", precision = 12, scale = 2)
    private BigDecimal interestAccrued;

    @Column(name = "deduction_total", precision = 12, scale = 2)
    private BigDecimal deductionTotal;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "return_amount", precision = 12, scale = 2)
    private BigDecimal returnAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- Business logic ----------

    public void addDeduction(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduction must be positive");
        }
        if (this.currentBalance == null) this.currentBalance = BigDecimal.ZERO;
        if (amount.compareTo(this.currentBalance) > 0) {
            throw new IllegalStateException("Deduction exceeds current balance");
        }
        this.currentBalance = this.currentBalance.subtract(amount);
        this.deductionTotal = (this.deductionTotal == null) ? amount : this.deductionTotal.add(amount);
    }

    public void addInterest(BigDecimal interest) {
        if (interest == null || interest.compareTo(BigDecimal.ZERO) <= 0) return;
        this.interestAccrued = (this.interestAccrued == null) ? interest : this.interestAccrued.add(interest);
        this.currentBalance = this.currentBalance.add(interest);
    }

    public void returnDeposit(BigDecimal returnAmount, LocalDate returnDate) {
        if (this.returnedDate != null) {
            throw new IllegalStateException("Deposit already returned");
        }
        if (returnAmount == null || returnAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Return amount must be >= 0");
        }
        if (returnAmount.compareTo(this.currentBalance) > 0) {
            throw new IllegalStateException("Return amount exceeds current balance");
        }
        this.returnAmount = returnAmount;
        this.returnedDate = (returnDate != null) ? returnDate : LocalDate.now();
        // Optionally set currentBalance = BigDecimal.ZERO to reflect depletion, but we keep it as is.
    }

    public boolean isActive() {
        return this.returnedDate == null;
    }

    public DepositStatus getDerivedStatus() {
        if (this.returnedDate != null) {
            return DepositStatus.RETURNED;
        } else if (this.deductionTotal != null && this.deductionTotal.compareTo(BigDecimal.ZERO) > 0) {
            return DepositStatus.PARTIALLY_DEDUCTED;
        } else {
            return DepositStatus.ACTIVE;
        }
    }
}
