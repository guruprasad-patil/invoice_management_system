package com.gsp.aiims.payment.controller;

import com.gsp.aiims.common.response.ApiResponse;
import com.gsp.aiims.payment.dto.PaymentRequest;
import com.gsp.aiims.payment.dto.PaymentResponse;
import com.gsp.aiims.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment recording and history")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Record a partial or full payment against an invoice")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully",
                        paymentService.recordPayment(request)));
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Get all payments for an invoice")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByInvoice(invoiceId)));
    }

    @GetMapping("/invoice/{invoiceId}/paged")
    @Operation(summary = "Get paginated payments for an invoice")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getByInvoicePaged(
            @PathVariable Long invoiceId,
            @PageableDefault(size = 10, sort = "paymentDate") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentsByInvoicePaged(invoiceId, pageable)));
    }
}
