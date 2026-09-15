package com.apex.mpesa.service;

import com.apex.mpesa.dto.*;
import java.util.List;

public interface DarajaService {

    PaymentInitiationResponseDto initiateStkPush(StkPushRequestDto request) throws Exception;

    void processCallback(MpesaCallbackDto callbackDto) throws Exception;

    TransactionStatusResponseDto queryTransactionStatus(String checkoutRequestId) throws Exception;

    TransactionResponseDto getTransactionById(String id) throws Exception;

    List<TransactionResponseDto> getTransactionsByTenant(String tenantId);

    List<TransactionResponseDto> getTransactionsByLease(String leaseId);
}

