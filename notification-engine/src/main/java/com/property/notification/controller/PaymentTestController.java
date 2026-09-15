package com.property.notification.controller;

import com.property.notification.dto.MaintenanceEventDto;
import com.property.notification.dto.PaymentEventDto;
import com.property.notification.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class PaymentTestController {

    private final EventPublisherService eventPublisherService;

    @PostMapping("/payment")
    public String simulatePayment(@RequestParam Long userId,
                                  @RequestParam String propertyId,
                                  @RequestParam BigDecimal amount,
                                  @RequestParam(defaultValue = "card") String method) {
        PaymentEventDto dto = PaymentEventDto.builder()
                .userId(userId)
                .propertyId(propertyId)
                .amount(amount)
                .paymentMethod(method)
                .build();
        eventPublisherService.publishPaymentEvent(dto);
        return "Payment event published";
    }

    @PostMapping("/maintenance")
    public String simulateMaintenance(@RequestParam Long userId,
                                      @RequestParam String propertyId,
                                      @RequestParam String description) {
        MaintenanceEventDto dto = MaintenanceEventDto.builder()
                .userId(userId)
                .propertyId(propertyId)
                .description(description)
                .build();
        eventPublisherService.publishMaintenanceEvent(dto);
        return "Maintenance event published";
    }
}
