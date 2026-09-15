package com.apex.auth.config;

import com.apex.auth.event.UserRegisteredEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the Auth Service.
 *
 * Declares:
 *  - ProducerFactory + KafkaTemplate  → publishing domain events
 *  - ConsumerFactory + ContainerFactory → consuming events (optional, for reactions)
 *  - KafkaAdmin + NewTopic beans      → auto-create topics on startup (dev only)
 *  - DefaultErrorHandler              → retry policy with backoff
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    // ============================================================
    // PRODUCER
    // ============================================================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Batching for throughput
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Do not add type headers — keeps payload stable across consumers
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ============================================================
    // CONSUMER
    // ============================================================

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // manual ack

        // Delegate deserialization errors to the ErrorHandlingDeserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JSON trusted packages — restrict for security
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.apex.auth.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserRegisteredEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Concurrency = number of partitions (or threads)
        factory.setConcurrency(3);

        // Manual ack so we only commit after successful processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Retry on failure: 3 attempts, 2s apart, then skip
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    // Dead-letter handling could go here
                    System.err.printf("Skipping record after retries: key=%s, topic=%s, err=%s%n",
                            record.key(), record.topic(), exception.getMessage());
                },
                new FixedBackOff(2000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    // ============================================================
    // TOPICS
    // ============================================================
    // NOTE: In production, disable KafkaAdmin auto-creation and create topics
    //       out-of-band (Terraform, kafka-topics.sh). Use these beans only in dev.

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(3)
                .replicas(1)                    // bump to 3 in production
                .config("retention.ms", String.valueOf(7L * 24 * 60 * 60 * 1000))  // 7 days
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "1")   // bump to 2 in production
                .build();
    }

    // ============================================================
    // ADMIN
    // ============================================================

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
