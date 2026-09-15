package com.apex.leaseService.consumer;

import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.auth.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEventConsumer {

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "lease-group")
    public void handleUserRegistered(UserRegisteredEvent event, Acknowledgment ack) {
        log.info("User registered: {} with role {}", event.getUserId(), event.getRole());
        // You can pre-populate tenant/landlord profiles, or validate existence before lease creation.
        // For example, cache the user details for future lease creation validations.
        ack.acknowledge();
    }
}