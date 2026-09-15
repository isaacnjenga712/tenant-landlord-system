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
    private String priority;          // LOW, MEDIUM, HIGH, URGENT
    private UUID correlationId;     // For tracing
}
