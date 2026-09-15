package com.apex.PaymentService.module.landlord.dto.request;

import com.apex.PaymentService.module.landlord.enums.LandlordStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LandlordUpdateDto {

    @Size(max = 255)
    private String companyName;

    @Size(max = 255)
    private String contactPerson;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    private LandlordStatus status;
}
