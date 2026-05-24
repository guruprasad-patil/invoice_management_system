package com.gsp.aiims.customer.service;

import com.gsp.aiims.audit.service.AuditService;
import com.gsp.aiims.common.enums.AuditAction;
import com.gsp.aiims.common.exception.DuplicateResourceException;
import com.gsp.aiims.common.exception.ResourceNotFoundException;
import com.gsp.aiims.customer.dto.CustomerRequest;
import com.gsp.aiims.customer.dto.CustomerResponse;
import com.gsp.aiims.customer.entity.Customer;
import com.gsp.aiims.customer.mapper.CustomerMapper;
import com.gsp.aiims.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (request.getEmail() != null && customerRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }
        Customer customer = customerMapper.toEntity(request);
        customer = customerRepository.save(customer);
        auditService.log(AuditAction.CREATE, "Customer", String.valueOf(customer.getId()), null, customer.getName());
        log.info("Customer created: id={}", customer.getId());
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findActiveOrThrow(id);

        if (request.getEmail() != null
                && customerRepository.existsByEmailAndDeletedFalseAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }

        String oldName = customer.getName();
        customerMapper.updateEntity(request, customer);
        customer = customerRepository.save(customer);
        auditService.log(AuditAction.UPDATE, "Customer", String.valueOf(id), oldName, customer.getName());
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = findActiveOrThrow(id);
        customer.setDeleted(true);
        customerRepository.save(customer);
        auditService.log(AuditAction.DELETE, "Customer", String.valueOf(id), customer.getName(), null);
        log.info("Customer soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String searchTerm, Pageable pageable) {
        return customerRepository.searchActive(searchTerm, pageable)
                .map(customerMapper::toResponse);
    }

    private Customer findActiveOrThrow(Long id) {
        return customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }
}
