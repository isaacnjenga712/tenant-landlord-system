package com.property.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MaintenanceRequestedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long propertyId;
    private final String description;

    public MaintenanceRequestedEvent(Object source, Long userId, Long propertyId, String description) {
        super(source);
        this.userId = userId;
        this.propertyId = propertyId;
        this.description = description;
    }
}
