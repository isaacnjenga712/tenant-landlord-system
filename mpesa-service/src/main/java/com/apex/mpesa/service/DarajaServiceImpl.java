package com.apex.mpesa.service;

import com.apex.mpesa.config.DarajaConfig;
import com.apex.mpesa.dto.*;
import com.apex.mpesa.entity.Transaction;
import com.apex.mpesa.entity.TransactionStatus;
import com.apex.mpesa.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DarajaServiceImpl implements DarajaService {

    private final DarajaConfig darajaConfig;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // -------- INITIATE STK PUSH --------
    @Override
    @Transactional
    public PaymentInitiationResponseDto initiateStkPush(StkPushRequestDto request) throws Exception {
        log.info("Initiating STK Push for tenant {} amount {}", request.getTenantId(), request.getAmount());

        String token = getOAuthToken();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = generatePassword(timestamp);

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", darajaConfig.getShortcode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", request.getAmount());
        body.put("PartyA", request.getPhone());
        body.put("PartyB", darajaConfig.getShortcode());
        body.put("PhoneNumber", request.getPhone());
        body.put("CallBackURL", darajaConfig.getCallbackUrl());
        body.put("AccountReference", request.getAccountReference() != null ? request.getAccountReference() : "RentPayment");
        body.put("TransactionDesc", "Rent Payment");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                darajaConfig.getStkpushUrl(), entity, JsonNode.class);

        JsonNode responseBody = response.getBody();

        Transaction transaction = new Transaction();
        transaction.setCheckoutRequestId(responseBody.get("CheckoutRequestID").asText());
        transaction.setMerchantRequestId(responseBody.get("MerchantRequestID").asText());
        transaction.setPhoneNumber(request.getPhone());
        // Convert amount from String to BigDecimal
        transaction.setAmount(new BigDecimal(request.getAmount()));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setAccountReference(request.getAccountReference());
        transaction.setTenantId(request.getTenantId());
        transaction.setLeaseId(request.getLeaseId());
        transaction.setInvoiceId(request.getInvoiceId());

        transactionRepository.save(transaction);

        PaymentInitiationResponseDto dto = new PaymentInitiationResponseDto();
        dto.setMerchantRequestId(responseBody.get("MerchantRequestID").asText());
        dto.setCheckoutRequestId(responseBody.get("CheckoutRequestID").asText());
        dto.setResponseCode(responseBody.get("ResponseCode").asText());
        dto.setResponseDescription(responseBody.get("ResponseDescription").asText());
        dto.setCustomerMessage(responseBody.get("CustomerMessage").asText());

        return dto;
    }

    // -------- PROCESS CALLBACK --------
    @Override
    @Transactional
    public void processCallback(MpesaCallbackDto callbackDto) throws Exception {
        MpesaCallbackDto.StkCallback stkCallback = callbackDto.getBody().getStkCallback();
        String checkoutRequestId = stkCallback.getCheckoutRequestID();
        int resultCode = stkCallback.getResultCode();
        String resultDesc = stkCallback.getResultDesc();

        log.info("Processing callback for CheckoutRequestID: {}", checkoutRequestId);

        Transaction transaction = transactionRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found for CheckoutRequestID: " + checkoutRequestId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Callback for {} ignored – status already {}", checkoutRequestId, transaction.getStatus());
            return;
        }

        transaction.setResultCode(resultCode);
        transaction.setResultDescription(resultDesc);

        if (resultCode == 0) {
            MpesaCallbackDto.CallbackMetadata metadata = stkCallback.getCallbackMetadata();
            if (metadata != null && metadata.getItem() != null) {
                for (MpesaCallbackDto.Item item : metadata.getItem()) {
                    String name = item.getName();
                    Object value = item.getValue();
                    if ("Amount".equals(name) && value instanceof Number) {
                        // Convert double to BigDecimal
                        transaction.setAmount(BigDecimal.valueOf(((Number) value).doubleValue()));
                    } else if ("MpesaReceiptNumber".equals(name)) {
                        transaction.setMpesaReceiptNumber(value.toString());
                    } else if ("PhoneNumber".equals(name) && value instanceof Number) {
                        transaction.setPhoneNumber(value.toString());
                    }
                }
            }
            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("Payment successful: Receipt {} for KES {} from {}",
                    transaction.getMpesaReceiptNumber(),
                    transaction.getAmount(),
                    transaction.getPhoneNumber());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.warn("Transaction failed: {} – {}", checkoutRequestId, resultDesc);
        }

        transactionRepository.save(transaction);
    }

    // -------- QUERY STATUS --------
    @Override
    public TransactionStatusResponseDto queryTransactionStatus(String checkoutRequestId) throws Exception {
        log.info("Querying status for CheckoutRequestID: {}", checkoutRequestId);

        String token = getOAuthToken();
        String url = darajaConfig.getBaseUrl() + "/mpesa/stkpushquery/v1/query";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = generatePassword(timestamp);

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", darajaConfig.getShortcode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("CheckoutRequestID", checkoutRequestId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

        JsonNode result = response.getBody();
        return objectMapper.treeToValue(result, TransactionStatusResponseDto.class);
    }

    // -------- GET TRANSACTION BY ID --------
    @Override
    public TransactionResponseDto getTransactionById(String id) throws Exception {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));
        return mapToDto(tx);
    }

    // -------- GET TRANSACTIONS BY TENANT --------
    @Override
    public List<TransactionResponseDto> getTransactionsByTenant(String tenantId) {
        return transactionRepository.findByTenantId(tenantId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // -------- GET TRANSACTIONS BY LEASE --------
    @Override
    public List<TransactionResponseDto> getTransactionsByLease(String leaseId) {
        return transactionRepository.findByLeaseId(leaseId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // -------- HELPERS --------
    private String getOAuthToken() {
        String auth = darajaConfig.getConsumerKey() + ":" + darajaConfig.getConsumerSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                darajaConfig.getOauthUrl(), HttpMethod.GET, entity, JsonNode.class);
        return response.getBody().get("access_token").asText();
    }

    private String generatePassword(String timestamp) {
        String toEncode = darajaConfig.getShortcode() + darajaConfig.getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
    }

    private TransactionResponseDto mapToDto(Transaction tx) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(tx.getId());
        dto.setCheckoutRequestId(tx.getCheckoutRequestId());
        dto.setMerchantRequestId(tx.getMerchantRequestId());
        dto.setPhoneNumber(tx.getPhoneNumber());
        // Convert BigDecimal to double for the DTO
        dto.setAmount(tx.getAmount() != null ? tx.getAmount().doubleValue() : null);
        dto.setMpesaReceiptNumber(tx.getMpesaReceiptNumber());
        dto.setResultDescription(tx.getResultDescription());
        dto.setResultCode(tx.getResultCode());
        dto.setStatus(tx.getStatus());
        dto.setAccountReference(tx.getAccountReference());
        dto.setTenantId(tx.getTenantId());
        dto.setLeaseId(tx.getLeaseId());
        dto.setInvoiceId(tx.getInvoiceId());
        dto.setCreatedAt(tx.getCreatedAt());
        dto.setUpdatedAt(tx.getUpdatedAt());
        return dto;
    }
}