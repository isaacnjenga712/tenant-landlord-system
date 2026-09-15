package com.property.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class RentPaymentReceivedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long propertyId;
    private final BigDecimal amount;
    private final String paymentMethod;

    public RentPaymentReceivedEvent(Object source, Long userId, Long propertyId,
                                    BigDecimal amount, String paymentMethod) {
        super(source);
        this.userId = userId;
        this.propertyId = propertyId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
