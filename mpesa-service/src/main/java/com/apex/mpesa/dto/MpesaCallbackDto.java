package com.apex.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaCallbackDto {
	
	private Body Body;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private StkCallback stkCallback;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StkCallback {
        private String MerchantRequestID;
        private String CheckoutRequestID;
        private int ResultCode;
        private String ResultDesc;
        private CallbackMetadata CallbackMetadata;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackMetadata {
        private List<Item> Item;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String Name;
        private Object Value;
    }

}
