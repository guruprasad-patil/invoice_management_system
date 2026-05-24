package com.gsp.aiims.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {

    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private int sortOrder;
}
