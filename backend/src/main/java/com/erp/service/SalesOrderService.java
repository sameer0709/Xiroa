package com.erp.service;

import com.erp.entity.Customer;
import com.erp.entity.SalesOrder;
import com.erp.enums.OrderStatus;
import com.erp.repository.CustomerRepository;
import com.erp.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;

    private static final DateTimeFormatter NUMBER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public SalesOrderService(SalesOrderRepository salesOrderRepository, CustomerRepository customerRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
    }

    public List<SalesOrder> getAll() {
        return salesOrderRepository.findAll();
    }

    @Transactional
    public SalesOrder create(Long customerId, String notes) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        SalesOrder so = new SalesOrder();
        so.setCustomer(customer);
        so.setSoNumber("SO-" + LocalDateTime.now().format(NUMBER_FMT));
        so.setOrderDate(LocalDateTime.now());
        so.setStatus(OrderStatus.PENDING);
        so.setNotes(notes);
        return salesOrderRepository.save(so);
    }
}
