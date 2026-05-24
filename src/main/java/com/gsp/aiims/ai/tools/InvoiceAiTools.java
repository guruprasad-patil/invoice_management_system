package com.gsp.aiims.ai.tools;

import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.invoice.repository.InvoiceRepository;
import com.gsp.aiims.report.dto.TopCustomerDto;
import com.gsp.aiims.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceAiTools {

    private final InvoiceRepository invoiceRepository;
    private final ReportService reportService;

    @Tool(description = "Get all unpaid invoices (status SENT or PARTIALLY_PAID). Returns count and total balance due.")
    public String getUnpaidInvoices() {
        var unpaid = invoiceRepository.findByStatusIn(
                List.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID));
        BigDecimal totalBalance = unpaid.stream()
                .map(i -> i.getBalanceDue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return String.format("Unpaid invoices: %d | Total balance due: $%s",
                unpaid.size(), totalBalance.toPlainString());
    }

    @Tool(description = "Get all overdue invoices (past due date and not yet paid). Returns count and total overdue amount.")
    public String getOverdueInvoices() {
        var overdue = invoiceRepository.findOverdue(InvoiceStatus.OVERDUE, LocalDate.now());
        BigDecimal totalOverdue = overdue.stream()
                .map(i -> i.getBalanceDue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return String.format("Overdue invoices: %d | Total overdue amount: $%s",
                overdue.size(), totalOverdue.toPlainString());
    }

    @Tool(description = "Get total revenue for the current month based on non-draft, non-cancelled invoices.")
    public String getMonthlyRevenue() {
        LocalDate today = LocalDate.now();
        BigDecimal revenue = reportService.getMonthlyRevenue(today.getYear(), today.getMonthValue());
        return String.format("Revenue for %s %d: $%s",
                today.getMonth().name(), today.getYear(), revenue.toPlainString());
    }

    @Tool(description = "Get total outstanding (unpaid) balance across all active invoices.")
    public String getOutstandingAmount() {
        BigDecimal outstanding = reportService.getOutstandingAmount();
        return String.format("Total outstanding amount: $%s", outstanding.toPlainString());
    }

    @Tool(description = "Get the top 5 customers ranked by total invoice revenue.")
    public String getTopCustomers() {
        List<TopCustomerDto> top = reportService.getTopCustomers(5);
        if (top.isEmpty()) {
            return "No customer revenue data available.";
        }
        StringBuilder sb = new StringBuilder("Top customers by revenue:\n");
        for (int i = 0; i < top.size(); i++) {
            TopCustomerDto c = top.get(i);
            sb.append(String.format("%d. %s — $%s%n",
                    i + 1, c.getCustomerName(), c.getTotalRevenue().toPlainString()));
        }
        return sb.toString();
    }
}
