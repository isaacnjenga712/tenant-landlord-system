package com.apex.module.lease.service.consumer;



import com.apex.module.lease.event.SagaReplyEvent;
import com.apex.module.lease.service.LeaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagaReplyConsumer {

    private final LeaseService leaseService;

    SagaReplyConsumer(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    @KafkaListener(topics = "lease-saga-replies", groupId = "lease-service-group")
    public void handleSagaReply(SagaReplyEvent reply) {
        leaseService.handleSagaReply(reply);
    }
}
