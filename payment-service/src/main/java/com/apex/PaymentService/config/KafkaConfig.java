package com.apex.PaymentService.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentConfirmedTopic() {
        return TopicBuilder.name("payment.events.confirmed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic mpesaStkRequestedTopic() {
        return TopicBuilder.name("mpesa.events.stk.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("payment.events.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }
}
