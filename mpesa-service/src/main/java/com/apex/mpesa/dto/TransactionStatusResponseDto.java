package com.apex.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusResponseDto {

	
	 private String ResponseCode;
	    private String ResponseDescription;
	    private String MerchantRequestID;
	    private String CheckoutRequestID;
	    private Integer ResultCode;
	    private String ResultDesc;
}
