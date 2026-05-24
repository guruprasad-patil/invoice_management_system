package com.gsp.aiims.report.controller;

import com.gsp.aiims.common.response.ApiResponse;
import com.gsp.aiims.report.dto.DashboardResponse;
import com.gsp.aiims.report.dto.MonthlyRevenueDto;
import com.gsp.aiims.report.dto.TopCustomerDto;
import com.gsp.aiims.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER')")
@Tag(name = "Reports", description = "Dashboard and financial reporting")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get full dashboard summary")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboard()));
    }

    @GetMapping("/revenue/monthly")
    @Operation(summary = "Get revenue for a specific month")
    public ResponseEntity<ApiResponse<BigDecimal>> getMonthlyRevenue(
            @RequestParam @Nullable Integer year,
            @RequestParam @Nullable Integer month) {
        LocalDate today = LocalDate.now();
        int y = year != null ? year : today.getYear();
        int m = month != null ? month : today.getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyRevenue(y, m)));
    }

    @GetMapping("/outstanding")
    @Operation(summary = "Get total outstanding (unpaid) balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getOutstanding() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOutstandingAmount()));
    }

    @GetMapping("/top-customers")
    @Operation(summary = "Get top customers by revenue")
    public ResponseEntity<ApiResponse<List<TopCustomerDto>>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getTopCustomers(limit)));
    }

    @GetMapping("/revenue/trend")
    @Operation(summary = "Get monthly revenue trend for the last N months")
    public ResponseEntity<ApiResponse<List<MonthlyRevenueDto>>> getRevenueTrend(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyRevenueTrend(months)));
    }
}
