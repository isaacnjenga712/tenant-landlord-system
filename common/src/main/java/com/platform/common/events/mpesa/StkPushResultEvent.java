package com.platform.common.events.mpesa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StkPushResultEvent {
    private String paymentId;
    private String checkoutRequestId;
    private String merchantRequestId;
    private String resultCode;
    private String resultDescription;
    private String mpesaReceiptNumber;
    private Instant completedAt;
}
