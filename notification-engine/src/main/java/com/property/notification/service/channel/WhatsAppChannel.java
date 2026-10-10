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
public class WhatsAppChannel implements NotificationChannel {

    private final WebClient.Builder webClientBuilder;

    @Value("${whatsapp.gateway.url:}")
    private String whatsappGatewayUrl;

    @Value("${whatsapp.from:}")
    private String whatsappFrom;

    @Override
    public String name() { return "WHATSAPP"; }

    @Override
    public boolean isOptedIn(UserPreference prefs) {
        return prefs.isWhatsappOptIn()
                && prefs.getWhatsappNumber() != null
                && !prefs.getWhatsappNumber().isBlank();
    }

    @Override
    public void send(UserPreference prefs, String subject, String body) {
        if (whatsappGatewayUrl == null || whatsappGatewayUrl.isBlank()) {
            log.info("[WHATSAPP MOCK] → {} :: {}", prefs.getWhatsappNumber(), body);
            return;
        }
        webClientBuilder.build()
                .post()
                .uri(whatsappGatewayUrl)
                .bodyValue(Map.of(
                        "from", whatsappFrom,
                        "to", prefs.getWhatsappNumber(),
                        "body", body
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.info("[WHATSAPP] → {} :: {}", prefs.getWhatsappNumber(), body);
    }
}