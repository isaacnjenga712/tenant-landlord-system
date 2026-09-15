package com.property.notification.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final WebClient.Builder webClientBuilder;

    @Value("${sms.gateway.url}")
    private String smsGatewayUrl;

    @Retry(name = "smsRetry")
    public void sendSms(String phoneNumber, String message) {
        log.info("Sending SMS to {}: {}", phoneNumber, message);
        webClientBuilder.build()
                .post()
                .uri(smsGatewayUrl)
                .bodyValue(Map.of("to", phoneNumber, "text", message))
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // Block for simplicity; consider async if needed
    }
}
