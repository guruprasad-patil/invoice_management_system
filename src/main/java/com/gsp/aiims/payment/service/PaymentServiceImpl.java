package com.gsp.aiims.payment.service;

import com.gsp.aiims.audit.service.AuditService;
import com.gsp.aiims.common.enums.AuditAction;
import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.common.exception.BusinessException;
import com.gsp.aiims.common.exception.ResourceNotFoundException;
import com.gsp.aiims.common.util.SecurityUtil;
import com.gsp.aiims.invoice.entity.Invoice;
import com.gsp.aiims.invoice.repository.InvoiceRepository;
import com.gsp.aiims.payment.dto.PaymentRequest;
import com.gsp.aiims.payment.dto.PaymentResponse;
import com.gsp.aiims.payment.entity.Payment;
import com.gsp.aiims.payment.mapper.PaymentMapper;
import com.gsp.aiims.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Set<InvoiceStatus> PAYABLE_STATUSES =
            EnumSet.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMapper paymentMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndDeletedFalse(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (!PAYABLE_STATUSES.contains(invoice.getStatus())) {
            throw new BusinessException("Invoice with status " + invoice.getStatus() + " cannot receive payments");
        }

        BigDecimal newPaid = invoice.getPaidAmount().add(request.getAmount());
        if (newPaid.compareTo(invoice.getTotalAmount()) > 0) {
            throw new BusinessException("Payment amount exceeds outstanding balance of "
                    + invoice.getBalanceDue());
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .createdBy(SecurityUtil.getCurrentUserId())
                .build();

        paymentRepository.save(payment);

        // Update invoice financial state
        invoice.setPaidAmount(newPaid);
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(newPaid));
        invoice.setStatus(deriveStatus(invoice));
        invoiceRepository.save(invoice);

        auditService.log(AuditAction.PAYMENT, "Invoice", String.valueOf(invoice.getId()),
                invoice.getPaidAmount().subtract(request.getAmount()).toPlainString(),
                newPaid.toPlainString());

        log.info("Payment recorded: invoiceId={}, amount={}", invoice.getId(), request.getAmount());
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId)
                .stream().map(paymentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByInvoicePaged(Long invoiceId, Pageable pageable) {
        return paymentRepository.findByInvoiceId(invoiceId, pageable)
                .map(paymentMapper::toResponse);
    }

    private InvoiceStatus deriveStatus(Invoice invoice) {
        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            return InvoiceStatus.PAID;
        }
        return InvoiceStatus.PARTIALLY_PAID;
    }
}
