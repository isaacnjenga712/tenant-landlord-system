package com.platform.common.events.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRegisteredEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID propertyId;
    private UUID landlordId;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String status;
    private Integer numberOfUnits;
    private UUID correlationId;
}
