package com.platform.common.events.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   // ✅ ADD THIS
public class TicketResolvedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID ticketId;
    private UUID unitId;
    private UUID tenantId;
    private String title;
    private UUID correlationId;
}
