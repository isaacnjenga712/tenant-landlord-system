package com.apex.auth.exception;

/**
 * Thrown when a domain event cannot be delivered to Kafka.
 *
 * HTTP status: 503 Service Unavailable
 * Reason: The service is healthy but a downstream dependency (Kafka) is not.
 *
 * Note: the default {@code KafkaEventPublisher} is fire-and-forget and logs
 * failures without throwing. This exception exists for callers that use the
 * synchronous send variant, or for the outbox pattern's dead-letter path.
 */
public class EventPublishException extends RuntimeException {

    private final String eventType;
    private final String topic;

    public EventPublishException(String eventType, String topic, Throwable cause) {
        super(String.format("Failed to publish event=%s to topic=%s: %s",
                eventType, topic, cause.getMessage()), cause);
        this.eventType = eventType;
        this.topic = topic;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTopic() {
        return topic;
    }
}
