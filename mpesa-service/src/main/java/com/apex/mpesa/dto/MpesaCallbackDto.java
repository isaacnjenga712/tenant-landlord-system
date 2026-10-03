package com.apex.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaCallbackDto {

    @JsonProperty("Body")
    private Body Body;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StkCallback {
        @JsonProperty("MerchantRequestID")
        private String MerchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String CheckoutRequestID;

        @JsonProperty("ResultCode")
        private int ResultCode;

        @JsonProperty("ResultDesc")
        private String ResultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata CallbackMetadata;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackMetadata {
        @JsonProperty("Item")
        private List<Item> Item;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("Name")
        private String Name;

        @JsonProperty("Value")
        private Object Value;
    }
}