package com.apex.PropertyManagementService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.apex.PropertyManagementService.entity.enums.LandlordStatus;

@Entity
@Table(name = "landlords")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Landlord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", columnDefinition = "UUID", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_person")
    private String contactPerson;

    private String phone;

    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Property> properties = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private LandlordStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (orgId == null) {
            throw new IllegalStateException("org_id is required — set TenantContext before saving");
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}