package com.gsp.aiims.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discountRate = BigDecimal.ZERO;

    private String notes;

    private String terms;

    @NotEmpty(message = "Invoice must have at least one item")
    @Valid
    private List<InvoiceItemRequest> items;
}
