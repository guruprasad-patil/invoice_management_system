package com.gsp.aiims.customer.controller;

import com.gsp.aiims.common.response.ApiResponse;
import com.gsp.aiims.customer.dto.CustomerRequest;
import com.gsp.aiims.customer.dto.CustomerResponse;
import com.gsp.aiims.customer.service.CustomerService;
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

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER')")
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER')")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Soft-delete a customer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "Search and list customers with pagination")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> search(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerService.search(search, pageable)));
    }
}
