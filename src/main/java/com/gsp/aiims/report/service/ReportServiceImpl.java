package com.gsp.aiims.report.service;

import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.customer.repository.CustomerRepository;
import com.gsp.aiims.invoice.repository.InvoiceRepository;
import com.gsp.aiims.report.dto.DashboardResponse;
import com.gsp.aiims.report.dto.MonthlyRevenueDto;
import com.gsp.aiims.report.dto.TopCustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        BigDecimal currentMonthRevenue = getMonthlyRevenue(today.getYear(), today.getMonthValue());
        BigDecimal outstandingAmount = getOutstandingAmount();
        long unpaidCount = invoiceRepository
                .findByStatusIn(List.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID)).size();
        long overdueCount = invoiceRepository
                .findOverdue(InvoiceStatus.OVERDUE, today).size();
        long activeCustomers = customerRepository.countActive();

        return DashboardResponse.builder()
                .currentMonthRevenue(currentMonthRevenue)
                .outstandingAmount(outstandingAmount)
                .totalActiveCustomers(activeCustomers)
                .totalUnpaidInvoices(unpaidCount)
                .totalOverdueInvoices(overdueCount)
                .topCustomers(getTopCustomers(5))
                .monthlyRevenue(getMonthlyRevenueTrend(12))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRevenue(int year, int month) {
        BigDecimal result = invoiceRepository.sumRevenueByMonth(year, month);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getOutstandingAmount() {
        BigDecimal result = invoiceRepository.sumOutstandingAmount();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopCustomerDto> getTopCustomers(int limit) {
        return invoiceRepository.findTopCustomersByRevenue(PageRequest.of(0, limit))
                .stream()
                .map(row -> new TopCustomerDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (BigDecimal) row[2]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueDto> getMonthlyRevenueTrend(int months) {
        LocalDate from = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        return invoiceRepository.findMonthlyRevenue(from)
                .stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int month = ((Number) row[1]).intValue();
                    BigDecimal revenue = (BigDecimal) row[2];
                    String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    return new MonthlyRevenueDto(year, month, monthName, revenue);
                })
                .toList();
    }
}
