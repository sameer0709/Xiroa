package com.erp.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull Long productId,
        @NotNull int quantity,
        String reason) {
}
