package com.erp.repository;

import com.erp.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    @Query("SELECT it.product.name AS product, SUM(it.quantity) AS qty, SUM(it.lineTotal) AS revenue " +
            "FROM InvoiceItem it JOIN it.invoice i " +
            "WHERE i.invoiceDate BETWEEN :start AND :end " +
            "GROUP BY it.product.name ORDER BY revenue DESC")
    List<Object[]> topProductsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('MONTH', i.invoiceDate) AS month, COALESCE(SUM(i.total),0) AS revenue " +
            "FROM Invoice i WHERE i.invoiceDate BETWEEN :start AND :end GROUP BY FUNCTION('MONTH', i.invoiceDate) ORDER BY month")
    List<Object[]> monthlyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
