package com.erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InvoiceItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity,
        BigDecimal unitPrice) {
}
