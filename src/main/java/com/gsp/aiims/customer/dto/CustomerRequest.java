package com.gsp.aiims.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Size(max = 255)
    private String company;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String taxId;

    private String notes;
}
