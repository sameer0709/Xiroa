package com.erp.service;

import com.erp.dto.DashboardSummary;
import com.erp.entity.Inventory;
import com.erp.entity.Payroll;
import com.erp.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;

    public DashboardService(InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            EmployeeRepository employeeRepository,
            PayrollRepository payrollRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
    }

    public DashboardSummary getSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime yearStart = today.withDayOfMonth(1).minusMonths(11).atStartOfDay();

        BigDecimal totalRevenue = invoiceRepository.sumTotalBetween(LocalDateTime.of(2000, 1, 1, 0, 0), now);
        BigDecimal monthlyRevenue = invoiceRepository.sumTotalBetween(monthStart, now);

        long totalInvoices = invoiceRepository.count();
        List<com.erp.entity.Invoice> outstanding = invoiceRepository.findOutstandingInvoices();
        BigDecimal outstandingAmount = outstanding.stream()
                .map(com.erp.entity.Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalProducts = productRepository.count();
        long lowStock = inventoryRepository.findByQuantityOnHandLessThanEqual(Integer.MAX_VALUE).stream()
                .filter(i -> i.getQuantityOnHand() <= i.getProduct().getReorderLevel())
                .count();

        BigDecimal inventoryValue = inventoryRepository.findAll().stream()
                .map(i -> i.getProduct().getCostPrice() == null ? BigDecimal.ZERO
                        : i.getProduct().getCostPrice().multiply(BigDecimal.valueOf(i.getQuantityOnHand())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCustomers = customerRepository.count();
        long totalEmployees = employeeRepository.count();

        // Payroll this month
        BigDecimal payrollThisMonth = payrollRepository.findByMonthAndYear(
                String.format("%02d", today.getMonthValue()), today.getYear())
                .stream().map(Payroll::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sales trend - last 6 months
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            BigDecimal total = invoiceRepository.sumTotalBetween(
                    ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", ym.format(DateTimeFormatter.ofPattern("MMM")));
            point.put("revenue", total);
            salesTrend.add(point);
        }

        // Top products (by revenue, last 12 months)
        List<Map<String, Object>> topProducts = new ArrayList<>();
        List<Object[]> productRows = invoiceItemRepository.topProductsBetween(yearStart, now);
        for (int i = 0; i < Math.min(5, productRows.size()); i++) {
            Object[] row = productRows.get(i);
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", row[0]);
            p.put("quantity", ((Number) row[1]).longValue());
            p.put("revenue", row[2]);
            topProducts.add(p);
        }

        // Top customers
        List<Map<String, Object>> topCustomers = new ArrayList<>();
        List<Object[]> customerRows = invoiceRepository.topCustomers();
        for (int i = 0; i < Math.min(5, customerRows.size()); i++) {
            Object[] row = customerRows.get(i);
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("name", row[0]);
            c.put("orders", ((Number) row[1]).longValue());
            c.put("revenue", row[2]);
            topCustomers.add(c);
        }

        return new DashboardSummary(
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                monthlyRevenue.setScale(2, RoundingMode.HALF_UP),
                (int) totalInvoices,
                outstanding.size(),
                outstandingAmount.setScale(2, RoundingMode.HALF_UP),
                (int) totalProducts,
                (int) lowStock,
                inventoryValue.setScale(2, RoundingMode.HALF_UP),
                (int) totalCustomers,
                (int) totalEmployees,
                payrollThisMonth.setScale(2, RoundingMode.HALF_UP),
                salesTrend,
                topProducts,
                topCustomers);
    }
}
