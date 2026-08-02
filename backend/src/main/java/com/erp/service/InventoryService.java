package com.erp.service;

import com.erp.dto.InventoryResponse;
import com.erp.dto.StockAdjustmentRequest;
import com.erp.entity.Inventory;
import com.erp.entity.Product;
import com.erp.entity.StockMovement;
import com.erp.enums.StockMovementType;
import com.erp.repository.InventoryRepository;
import com.erp.repository.ProductRepository;
import com.erp.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<InventoryResponse> lowStock() {
        return inventoryRepository.findByQuantityOnHandLessThanEqual(Integer.MAX_VALUE).stream()
                .filter(i -> i.getQuantityOnHand() <= i.getProduct().getReorderLevel())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InventoryResponse adjust(StockAdjustmentRequest req) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Inventory inv = inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setProduct(product);
                    i.setQuantityOnHand(0);
                    return i;
                });

        int newQty = inv.getQuantityOnHand() + req.quantity();
        if (newQty < 0) {
            throw new RuntimeException("Insufficient stock to adjust by " + req.quantity());
        }
        inv.setQuantityOnHand(newQty);
        inventoryRepository.save(inv);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(StockMovementType.ADJUSTMENT);
        movement.setQuantity(Math.abs(req.quantity()));
        movement.setUnitCost(product.getCostPrice());
        movement.setReference("MANUAL");
        movement.setNotes(req.reason());
        stockMovementRepository.save(movement);

        return toResponse(inv);
    }

    private InventoryResponse toResponse(Inventory inv) {
        Product p = inv.getProduct();
        BigDecimal stockValue = p.getCostPrice() == null ? BigDecimal.ZERO
                : p.getCostPrice().multiply(BigDecimal.valueOf(inv.getQuantityOnHand()));
        return new InventoryResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getCategory() == null ? "Uncategorized" : p.getCategory().getName(),
                inv.getQuantityOnHand(),
                inv.getAvailableQuantity(),
                p.getReorderLevel(),
                p.getSellingPrice(),
                stockValue,
                inv.getQuantityOnHand() <= p.getReorderLevel());
    }
}
