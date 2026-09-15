package com.apex.mpesa.dto;

import lombok.Data;

@Data
public class PaymentInitiationResponseDto {
	
	private String merchantRequestId;
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;

}
