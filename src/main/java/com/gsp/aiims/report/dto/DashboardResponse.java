package com.gsp.aiims.report.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private BigDecimal currentMonthRevenue;
    private BigDecimal outstandingAmount;
    private long totalActiveCustomers;
    private long totalUnpaidInvoices;
    private long totalOverdueInvoices;
    private List<TopCustomerDto> topCustomers;
    private List<MonthlyRevenueDto> monthlyRevenue;
}
