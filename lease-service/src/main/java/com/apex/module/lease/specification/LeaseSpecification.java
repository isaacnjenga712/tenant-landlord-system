package com.apex.module.lease.specification;

import com.apex.module.lease.enums.LeaseStatus;
import com.apex.module.lease.entity.Lease;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;


public class LeaseSpecification {
	
	 public static Specification<Lease> hasTenantId(UUID tenantId) {
	        return (root, query, cb) -> tenantId == null ? cb.conjunction() : cb.equal(root.get("tenantId"), tenantId);
	    }

	    public static Specification<Lease> hasLandlordId(UUID landlordId) {
	        return (root, query, cb) -> landlordId == null ? cb.conjunction() : cb.equal(root.get("landlordId"), landlordId);
	    }

	    public static Specification<Lease> hasPropertyId(UUID propertyId) {
	        return (root, query, cb) -> propertyId == null ? cb.conjunction() : cb.equal(root.get("propertyId"), propertyId);
	    }

	    public static Specification<Lease> hasStatus(LeaseStatus status) {
	        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	    }

	    public static Specification<Lease> hasStartDateAfterOrEqual(LocalDate startDate) {
	        return (root, query, cb) -> startDate == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
	    }

	    public static Specification<Lease> hasEndDateBeforeOrEqual(LocalDate endDate) {
	        return (root, query, cb) -> endDate == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("endDate"), endDate);
	    }

}
