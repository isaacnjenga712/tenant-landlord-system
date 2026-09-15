package com.property.notification.service;

import com.property.notification.dto.MaintenanceEventDto;
import com.property.notification.dto.PaymentEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment}")
    private String paymentTopic;

    @Value("${app.kafka.topics.maintenance}")
    private String maintenanceTopic;

    public void publishPaymentEvent(PaymentEventDto event) {
        kafkaTemplate.send(paymentTopic, event.getUserId().toString(), event);
    }

    public void publishMaintenanceEvent(MaintenanceEventDto event) {
        kafkaTemplate.send(maintenanceTopic, event.getUserId().toString(), event);
    }
}
