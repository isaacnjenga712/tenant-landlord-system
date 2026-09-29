package com.platform.common.events.maintenance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID ticketId;
    private UUID unitId;
    private UUID tenantId;
    private String title;
    private String priority;
    private UUID correlationId;
    private String eventType;

    public TicketCreatedEvent(UUID ticketId, UUID unitId, UUID tenantId,
                              String title, String priority, UUID correlationId) {
        this.ticketId = ticketId;
        this.unitId = unitId;
        this.tenantId = tenantId;
        this.title = title;
        this.priority = priority;
        this.correlationId = correlationId;
    }
}
