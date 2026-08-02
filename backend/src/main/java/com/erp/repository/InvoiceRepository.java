package com.erp.repository;

import com.erp.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByInvoiceDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.invoiceDate BETWEEN :start AND :end")
    BigDecimal sumTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT i.customer.name AS customer, COUNT(i) AS orders, COALESCE(SUM(i.total),0) AS revenue " +
            "FROM Invoice i GROUP BY i.customer.name ORDER BY revenue DESC")
    List<Object[]> topCustomers();

    @Query("SELECT i FROM Invoice i WHERE i.paymentStatus = 'UNPAID' OR i.paymentStatus = 'PARTIALLY_PAID'")
    List<Invoice> findOutstandingInvoices();
}
