package com.apex.mpesa.dto;

import com.apex.mpesa.entity.TransactionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponseDto {

    private String id;
    private String checkoutRequestId;
    private String merchantRequestId;
    private String phoneNumber;
    private Double amount;
    private String mpesaReceiptNumber;
    private String resultDescription;
    private Integer resultCode;
    private TransactionStatus status;
    private String accountReference;

    private String tenantId;
    private String leaseId;
    private String invoiceId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
