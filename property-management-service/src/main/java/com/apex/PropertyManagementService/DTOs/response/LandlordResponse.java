package com.apex.PropertyManagementService.DTOs.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LandlordResponse {

	 private UUID id;
	    private String firstName;
	    private String lastName;
	    private String email;
	    private String phoneNumber;
	    private String status;
	    private List<UUID> propertyIds;
	} 