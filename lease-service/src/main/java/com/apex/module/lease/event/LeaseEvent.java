package com.apex.module.lease.event;

import com.apex.module.lease.enums.LeaseEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public class LeaseEvent {
    private UUID eventId;
    private LeaseEventType eventType;
    private LocalDateTime timestamp;
    private String source;
    private LeaseEventPayload payload;

    public LeaseEvent() {}

    public LeaseEvent(UUID eventId, LeaseEventType eventType, LocalDateTime timestamp,
                      String source, LeaseEventPayload payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.source = source;
        this.payload = payload;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public LeaseEventType getEventType() { return eventType; }
    public void setEventType(LeaseEventType eventType) { this.eventType = eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LeaseEventPayload getPayload() { return payload; }
    public void setPayload(LeaseEventPayload payload) { this.payload = payload; }
}
