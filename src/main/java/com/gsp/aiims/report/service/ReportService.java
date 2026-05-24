package com.gsp.aiims.report.service;

import com.gsp.aiims.report.dto.DashboardResponse;
import com.gsp.aiims.report.dto.MonthlyRevenueDto;
import com.gsp.aiims.report.dto.TopCustomerDto;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    DashboardResponse getDashboard();

    BigDecimal getMonthlyRevenue(int year, int month);

    BigDecimal getOutstandingAmount();

    List<TopCustomerDto> getTopCustomers(int limit);

    List<MonthlyRevenueDto> getMonthlyRevenueTrend(int months);
}
