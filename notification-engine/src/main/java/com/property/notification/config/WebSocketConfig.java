package com.property.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to /topic/... and /queue/...
        config.enableSimpleBroker("/topic", "/queue");
        // Messages sent from client go to /app/...
        config.setApplicationDestinationPrefixes("/app");
        // User-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Handshake endpoint — clients connect to ws://host/ws/notifications
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")   // tighten in prod
                .withSockJS();                   // fallback for browsers without WS
    }
}
