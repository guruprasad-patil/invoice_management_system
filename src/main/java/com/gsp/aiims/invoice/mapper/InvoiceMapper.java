package com.gsp.aiims.invoice.mapper;

import com.gsp.aiims.invoice.dto.InvoiceItemRequest;
import com.gsp.aiims.invoice.dto.InvoiceItemResponse;
import com.gsp.aiims.invoice.dto.InvoiceResponse;
import com.gsp.aiims.invoice.entity.Invoice;
import com.gsp.aiims.invoice.entity.InvoiceItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    InvoiceResponse toResponse(Invoice invoice);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    InvoiceItem toItemEntity(InvoiceItemRequest request);
}
