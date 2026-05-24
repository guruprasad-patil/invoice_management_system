package com.gsp.aiims.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyRevenueDto {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal revenue;
}
