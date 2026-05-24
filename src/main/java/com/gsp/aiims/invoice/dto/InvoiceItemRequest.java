package com.gsp.aiims.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemRequest {

    @NotBlank(message = "Item description is required")
    @Size(max = 500)
    private String description;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discountRate = BigDecimal.ZERO;

    private int sortOrder = 0;
}
