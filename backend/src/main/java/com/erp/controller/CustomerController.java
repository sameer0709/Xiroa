package com.erp.controller;

import com.erp.dto.CustomerRequest;
import com.erp.dto.IdNameResponse;
import com.erp.dto.VendorRequest;
import com.erp.entity.Customer;
import com.erp.entity.Vendor;
import com.erp.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/id-names")
    public ResponseEntity<List<IdNameResponse>> getIdNames() {
        return ResponseEntity.ok(customerService.getCustomerIdNames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Customer> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // ---- Vendors ----
    @GetMapping("/vendors/all")
    public ResponseEntity<List<Vendor>> getVendors() {
        return ResponseEntity.ok(customerService.getAllVendors());
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<IdNameResponse>> getVendorIdNames() {
        return ResponseEntity.ok(customerService.getVendorIdNames());
    }

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Vendor> createVendor(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createVendor(request));
    }
}
