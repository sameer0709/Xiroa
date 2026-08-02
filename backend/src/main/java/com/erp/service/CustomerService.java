package com.erp.service;

import com.erp.dto.CustomerRequest;
import com.erp.dto.IdNameResponse;
import com.erp.dto.VendorRequest;
import com.erp.entity.Customer;
import com.erp.entity.Vendor;
import com.erp.repository.CustomerRepository;
import com.erp.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;

    public CustomerService(CustomerRepository customerRepository, VendorRepository vendorRepository) {
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
    }

    // ---- Customers ----
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    public List<IdNameResponse> getCustomerIdNames() {
        return customerRepository.findAll().stream()
                .map(c -> new IdNameResponse(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public Customer createCustomer(CustomerRequest req) {
        Customer c = new Customer();
        apply(c, req);
        return customerRepository.save(c);
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest req) {
        Customer c = getCustomer(id);
        apply(c, req);
        return customerRepository.save(c);
    }

    private void apply(Customer c, CustomerRequest req) {
        c.setName(req.name());
        c.setGstin(req.gstin());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setCity(req.city());
        c.setState(req.state());
        c.setAddress(req.address());
        c.setCompany(req.company());
    }

    // ---- Vendors ----
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<IdNameResponse> getVendorIdNames() {
        return vendorRepository.findAll().stream()
                .map(v -> new IdNameResponse(v.getId(), v.getName()))
                .toList();
    }

    @Transactional
    public Vendor createVendor(VendorRequest req) {
        Vendor v = new Vendor();
        applyVendor(v, req);
        return vendorRepository.save(v);
    }

    private void applyVendor(Vendor v, VendorRequest req) {
        v.setName(req.name());
        v.setGstin(req.gstin());
        v.setContactPerson(req.contactPerson());
        v.setPhone(req.phone());
        v.setEmail(req.email());
        v.setAddress(req.address());
        v.setCity(req.city());
        v.setState(req.state());
    }
}
