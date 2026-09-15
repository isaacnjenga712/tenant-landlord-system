package com.apex.PaymentService.module.landlord.entity;

import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "landlords")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Landlord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_name", length = 255, nullable = false)
    private String companyName;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LandlordStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}