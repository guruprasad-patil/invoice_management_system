package com.gsp.aiims.customer.service;

import com.gsp.aiims.customer.dto.CustomerRequest;
import com.gsp.aiims.customer.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);

    CustomerResponse getById(Long id);

    Page<CustomerResponse> search(String searchTerm, Pageable pageable);
}
