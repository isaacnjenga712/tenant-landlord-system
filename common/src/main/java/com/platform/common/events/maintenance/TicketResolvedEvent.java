package com.platform.common.events.maintenance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResolvedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID ticketId;
    private UUID unitId;
    private UUID tenantId;
    private String title;
    private UUID correlationId;
    private String eventType;         // e.g. "maintenance.ticket.resolved"

    public TicketResolvedEvent(UUID ticketId, UUID unitId, UUID tenantId,
                               String title, UUID correlationId) {
        this.ticketId = ticketId;
        this.unitId = unitId;
        this.tenantId = tenantId;
        this.title = title;
        this.correlationId = correlationId;
    }
}
