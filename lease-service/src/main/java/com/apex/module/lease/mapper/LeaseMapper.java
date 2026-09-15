package com.apex.module.lease.mapper;

import com.apex.module.lease.dto.request.LeaseCreateRequest;
import com.apex.module.lease.dto.response.LeaseResponse;
import com.apex.module.lease.entity.Lease;
import org.springframework.stereotype.Component;

@Component
public class LeaseMapper {
	
	 public Lease toEntity(LeaseCreateRequest request) {
	        Lease lease = new Lease();
	        lease.setPropertyId(request.getPropertyId());
	        lease.setTenantId(request.getTenantId());
	        lease.setLandlordId(request.getLandlordId());
	        lease.setStartDate(request.getStartDate());
	        lease.setEndDate(request.getEndDate());
	        lease.setRentAmount(request.getRentAmount());
	        lease.setDepositAmount(request.getDepositAmount());
	        lease.setTermsAndConditions(request.getTermsAndConditions());
	        // status is set by the service
	        return lease;
	    }

	    public LeaseResponse toResponse(Lease lease) {
	        LeaseResponse response = new LeaseResponse();
	        response.setId(lease.getId());
	        response.setPropertyId(lease.getPropertyId());
	        response.setTenantId(lease.getTenantId());
	        response.setLandlordId(lease.getLandlordId());
	        response.setStartDate(lease.getStartDate());
	        response.setEndDate(lease.getEndDate());
	        response.setRentAmount(lease.getRentAmount());
	        response.setDepositAmount(lease.getDepositAmount());
	        response.setStatus(lease.getStatus());
	        response.setTermsAndConditions(lease.getTermsAndConditions());
	        response.setCreatedAt(lease.getCreatedAt());
	        response.setUpdatedAt(lease.getUpdatedAt());
	        return response;
	    }

}
