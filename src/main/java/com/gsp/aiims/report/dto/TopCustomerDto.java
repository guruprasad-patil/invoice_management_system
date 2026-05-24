package com.gsp.aiims.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopCustomerDto {

    private Long customerId;
    private String customerName;
    private BigDecimal totalRevenue;
}
