package com.gsp.aiims.invoice.service;

import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.invoice.dto.InvoiceRequest;
import com.gsp.aiims.invoice.dto.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

    InvoiceResponse create(InvoiceRequest request);

    InvoiceResponse update(Long id, InvoiceRequest request);

    void delete(Long id);

    InvoiceResponse getById(Long id);

    Page<InvoiceResponse> search(String search, InvoiceStatus status, Long customerId, Pageable pageable);

    InvoiceResponse updateStatus(Long id, InvoiceStatus newStatus);
}
