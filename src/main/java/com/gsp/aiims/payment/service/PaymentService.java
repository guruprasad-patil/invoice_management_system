package com.gsp.aiims.payment.service;

import com.gsp.aiims.payment.dto.PaymentRequest;
import com.gsp.aiims.payment.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    PaymentResponse recordPayment(PaymentRequest request);

    List<PaymentResponse> getPaymentsByInvoice(Long invoiceId);

    Page<PaymentResponse> getPaymentsByInvoicePaged(Long invoiceId, Pageable pageable);
}
