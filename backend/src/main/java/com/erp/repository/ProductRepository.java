package com.erp.repository;

import com.erp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.id IN " +
            "(SELECT i.product.id FROM Inventory i WHERE i.quantityOnHand <= p.reorderLevel)")
    List<Product> findLowStockProducts();
}
