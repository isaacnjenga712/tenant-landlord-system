package com.property.notification.service.channel;

import com.property.notification.domain.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsChannel implements NotificationChannel {

    private final WebClient.Builder webClientBuilder;

    @Value("${sms.gateway.url:}")
    private String smsGatewayUrl;

    @Override
    public String name() { return "SMS"; }

    @Override
    public boolean isOptedIn(UserPreference prefs) {
        return prefs.isSmsOptIn() && prefs.getPhone() != null && !prefs.getPhone().isBlank();
    }

    @Override
    public void send(UserPreference prefs, String subject, String body) {
        if (smsGatewayUrl == null || smsGatewayUrl.isBlank()) {
            log.info("[SMS MOCK] → {} :: {}", prefs.getPhone(), body);
            return;
        }
        webClientBuilder.build()
                .post()
                .uri(smsGatewayUrl)
                .bodyValue(Map.of("to", prefs.getPhone(), "text", body))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.info("[SMS] → {} :: {}", prefs.getPhone(), body);
    }
}
