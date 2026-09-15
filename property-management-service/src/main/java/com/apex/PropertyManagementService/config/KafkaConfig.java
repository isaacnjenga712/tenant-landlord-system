package com.apex.PropertyManagementService.config;

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
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic for property.registered events.
     * This topic will be auto-created on application startup if it does not exist.
     */
    @Bean
    public NewTopic propertyRegisteredTopic() {
        return TopicBuilder.name(KafkaTopics.PROPERTY_REGISTERED)
                .partitions(3)
                .replicas(1)
                .config("min.insync.replicas", "1")
                .build();
    }

    /**
     * (Optional) Dead Letter Topic for failed events.
     * You can uncomment this if you want to handle retries.
     */
    // @Bean
    // public NewTopic propertyRegisteredDltTopic() {
    //     return TopicBuilder.name(KafkaTopics.PROPERTY_REGISTERED + "-dlt")
    //             .partitions(3)
    //             .replicas(1)
    //             .build();
    // }
}
