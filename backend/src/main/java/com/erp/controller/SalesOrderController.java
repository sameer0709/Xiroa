package com.erp.controller;

import com.erp.entity.SalesOrder;
import com.erp.service.SalesOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<SalesOrder>> getAll() {
        return ResponseEntity.ok(salesOrderService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SalesOrder> create(@RequestParam Long customerId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesOrderService.create(customerId, notes));
    }
}
