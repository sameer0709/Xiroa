package com.erp.dto;

import java.math.BigDecimal;

public record InventoryResponse(
        Long productId,
        String sku,
        String productName,
        String category,
        int quantityOnHand,
        int availableQuantity,
        int reorderLevel,
        BigDecimal sellingPrice,
        BigDecimal stockValue,
        boolean lowStock) {
}
