package com.gsp.aiims.invoice.service;

import com.gsp.aiims.audit.service.AuditService;
import com.gsp.aiims.common.enums.AuditAction;
import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.common.exception.BusinessException;
import com.gsp.aiims.common.exception.ResourceNotFoundException;
import com.gsp.aiims.customer.entity.Customer;
import com.gsp.aiims.customer.repository.CustomerRepository;
import com.gsp.aiims.invoice.dto.InvoiceItemRequest;
import com.gsp.aiims.invoice.dto.InvoiceRequest;
import com.gsp.aiims.invoice.dto.InvoiceResponse;
import com.gsp.aiims.invoice.entity.Invoice;
import com.gsp.aiims.invoice.entity.InvoiceItem;
import com.gsp.aiims.invoice.mapper.InvoiceMapper;
import com.gsp.aiims.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final Set<InvoiceStatus> IMMUTABLE_STATUSES =
            EnumSet.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED);

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceMapper invoiceMapper;
    private final AuditService auditService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Customer customer = findActiveCustomer(request.getCustomerId());
        validateDates(request.getIssueDate(), request.getDueDate());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customer(customer)
                .status(InvoiceStatus.DRAFT)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .taxRate(nvl(request.getTaxRate()))
                .discountRate(nvl(request.getDiscountRate()))
                .notes(request.getNotes())
                .terms(request.getTerms())
                .build();

        populateItems(invoice, request.getItems());
        recalculateTotals(invoice);

        invoice = invoiceRepository.save(invoice);
        auditService.log(AuditAction.CREATE, "Invoice", String.valueOf(invoice.getId()),
                null, invoice.getInvoiceNumber());
        log.info("Invoice created: {}", invoice.getInvoiceNumber());
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse update(Long id, InvoiceRequest request) {
        Invoice invoice = findActiveOrThrow(id);

        if (IMMUTABLE_STATUSES.contains(invoice.getStatus())) {
            throw new BusinessException("Cannot modify a " + invoice.getStatus() + " invoice");
        }

        Customer customer = findActiveCustomer(request.getCustomerId());
        validateDates(request.getIssueDate(), request.getDueDate());

        String oldNumber = invoice.getInvoiceNumber();
        invoice.setCustomer(customer);
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setTaxRate(nvl(request.getTaxRate()));
        invoice.setDiscountRate(nvl(request.getDiscountRate()));
        invoice.setNotes(request.getNotes());
        invoice.setTerms(request.getTerms());

        invoice.clearItems();
        populateItems(invoice, request.getItems());
        recalculateTotals(invoice);

        invoice = invoiceRepository.save(invoice);
        auditService.log(AuditAction.UPDATE, "Invoice", String.valueOf(id), oldNumber, invoice.getInvoiceNumber());
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Invoice invoice = findActiveOrThrow(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot delete a paid invoice");
        }
        invoice.setDeleted(true);
        invoiceRepository.save(invoice);
        auditService.log(AuditAction.DELETE, "Invoice", String.valueOf(id), invoice.getInvoiceNumber(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        return invoiceMapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> search(String search, InvoiceStatus status, Long customerId, Pageable pageable) {
        return invoiceRepository.search(search, status, customerId, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Override
    @Transactional
    public InvoiceResponse updateStatus(Long id, InvoiceStatus newStatus) {
        Invoice invoice = findActiveOrThrow(id);
        InvoiceStatus old = invoice.getStatus();
        invoice.setStatus(newStatus);
        invoiceRepository.save(invoice);
        auditService.log(AuditAction.UPDATE, "Invoice", String.valueOf(id), old.name(), newStatus.name());
        return invoiceMapper.toResponse(invoice);
    }

    // ---- internal helpers ----

    private void populateItems(Invoice invoice, List<InvoiceItemRequest> itemRequests) {
        for (int i = 0; i < itemRequests.size(); i++) {
            InvoiceItemRequest req = itemRequests.get(i);
            InvoiceItem item = invoiceMapper.toItemEntity(req);
            item.setSortOrder(i);
            calculateItemAmounts(item);
            invoice.addItem(item);
        }
    }

    private void calculateItemAmounts(InvoiceItem item) {
        BigDecimal base = item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmt = base.multiply(item.getDiscountRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = base.subtract(discountAmt);
        BigDecimal taxAmt = afterDiscount.multiply(item.getTaxRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        item.setDiscountAmount(discountAmt);
        item.setTaxAmount(taxAmt);
        item.setTotalAmount(afterDiscount.add(taxAmt));
    }

    private void recalculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(InvoiceItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmt = subtotal.multiply(invoice.getDiscountRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discountAmt);
        BigDecimal taxAmt = afterDiscount.multiply(invoice.getTaxRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(taxAmt);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmt);
        invoice.setTaxAmount(taxAmt);
        invoice.setTotalAmount(total);
        invoice.setBalanceDue(total.subtract(invoice.getPaidAmount()));
    }

    private String generateInvoiceNumber() {
        Long seq = jdbcTemplate.queryForObject("SELECT nextval('invoice_number_seq')", Long.class);
        return String.format("INV-%d-%06d", LocalDate.now().getYear(), seq);
    }

    private Customer findActiveCustomer(Long customerId) {
        return customerRepository.findByIdAndDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    private Invoice findActiveOrThrow(Long id) {
        return invoiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private void validateDates(LocalDate issueDate, LocalDate dueDate) {
        if (dueDate.isBefore(issueDate)) {
            throw new BusinessException("Due date must not be before issue date");
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
