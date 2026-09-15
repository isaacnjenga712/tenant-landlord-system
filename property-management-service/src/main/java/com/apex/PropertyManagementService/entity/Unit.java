package com.apex.PropertyManagementService.entity;

import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String unitNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;

    @Enumerated(EnumType.STRING)
    private UnitStatus status;

    private String currentTenantId;  // Reference to tenant (UUID as String)

    // auditing fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
			
				