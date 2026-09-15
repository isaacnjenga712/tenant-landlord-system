package com.apex.maintenanceTicketService.controller;

import com.apex.maintenanceTicketService.dto.request.TicketCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TicketUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TicketResponse;
import com.apex.maintenanceTicketService.service.TicketCommandService;
import com.apex.maintenanceTicketService.service.TicketQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketCommandService commandService;
    private final TicketQueryService queryService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId,
            @RequestHeader(value = "X-Landlord-ID", required = false) UUID landlordId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createTicket(request, tenantId, landlordId));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(queryService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.getTicketById(id));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(queryService.getTicketsByUnit(unitId));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(queryService.getTicketsByTenant(tenantId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponse>> getTicketsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(queryService.getTicketsByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(commandService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(commandService.updateTicketStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        commandService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
