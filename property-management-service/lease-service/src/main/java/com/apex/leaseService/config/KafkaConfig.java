package com.apex.leaseService.config;

import com.platform.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * KafkaAdmin bean to manage topics programmatically.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Optional: set timeout, etc.
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic for lease.created events.
     */
    @Bean
    public NewTopic leaseCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.LEASE_CREATED)
                .partitions(3)          // Adjust based on expected throughput
                .replicas(1)            // Set to 2+ in production for high availability
                .config("min.insync.replicas", "1")  // Optional: ensures durability
                .build();
    }

    /**
     * Topic for lease.terminated events.
     */
    @Bean
    public NewTopic leaseTerminatedTopic() {
        return TopicBuilder.name(KafkaTopics.LEASE_TERMINATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic for lease.compensation.required events.
     */
    @Bean
    public NewTopic leaseCompensationTopic() {
        return TopicBuilder.name(KafkaTopics.LEASE_COMPENSATION_REQUIRED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * (Optional) Dead Letter Topics for retry handling.
     * You can also create them manually if needed.
     */
    @Bean
    public NewTopic leaseCreatedDltTopic() {
        return TopicBuilder.name(KafkaTopics.LEASE_CREATED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentReceivedDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_RECEIVED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
