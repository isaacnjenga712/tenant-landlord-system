package com.apex.mpesa.controller;

import com.apex.mpesa.dto.*;
import jakarta.validation.Valid;
import com.apex.mpesa.service.DarajaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mpesa")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

	private final DarajaService darajaService;

    // -------- Initiate STK Push --------
    @PostMapping("/stk-push")
    public ResponseEntity<?> initiateStkPush(@Valid @RequestBody StkPushRequestDto request) {
        try {
            PaymentInitiationResponseDto response = darajaService.initiateStkPush(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("STK Push initiation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to initiate payment. Please try again later."));
        }
    }

    // -------- Safaricom Callback (Webhook) --------
    @PostMapping("/callback")
    public ResponseEntity<?> mpesaCallback(@RequestBody MpesaCallbackDto callbackPayload) {
        log.info("=== CALLBACK HIT ===");
        log.info("Payload: {}", callbackPayload);
        try {
            darajaService.processCallback(callbackPayload);
            log.info("=== CALLBACK PROCESSED OK ===");
            return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDescription", "Success"));
        } catch (Exception e) {
            log.error("=== CALLBACK PROCESSING ERROR ===", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ResultCode", 1, "ResultDescription", "Internal error"));
        }
    }

    // -------- Query Transaction Status from Safaricom --------
    @PostMapping("/query-status")
    public ResponseEntity<?> queryTransactionStatus(@RequestBody TransactionStatusQueryDto queryDto) {
        try {
            TransactionStatusResponseDto status = darajaService.queryTransactionStatus(queryDto.getCheckoutRequestId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Status query error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query transaction status."));
        }
    }

    // -------- Get Transaction by internal UUID --------
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable String id) {
        try {
            TransactionResponseDto dto = darajaService.getTransactionById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching transaction {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve transaction."));
        }
    }

    @GetMapping("/transactions/tenant/{tenantId}")
    public ResponseEntity<?> getTransactionsByTenant(@PathVariable String tenantId) {
        try {
            List<TransactionResponseDto> transactions = darajaService.getTransactionsByTenant(tenantId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error fetching transactions for tenant {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve tenant transactions."));
        }
    }

    @GetMapping("/transactions/lease/{leaseId}")
    public ResponseEntity<?> getTransactionsByLease(@PathVariable String leaseId) {
        try {
            List<TransactionResponseDto> transactions = darajaService.getTransactionsByLease(leaseId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error fetching transactions for lease {}", leaseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve lease transactions."));
        }
    }
}