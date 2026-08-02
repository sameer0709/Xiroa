package com.erp.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
        BigDecimal totalRevenue,
        BigDecimal monthlyRevenue,
        int totalInvoices,
        int outstandingInvoices,
        BigDecimal outstandingAmount,
        int totalProducts,
        int lowStockProducts,
        BigDecimal inventoryValue,
        int totalCustomers,
        int totalEmployees,
        BigDecimal payrollThisMonth,
        List<Map<String, Object>> salesTrend,
        List<Map<String, Object>> topProducts,
        List<Map<String, Object>> topCustomers) {
}
