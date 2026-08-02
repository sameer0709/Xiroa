package com.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseOrderRequest(
        @NotNull Long vendorId,
        @NotEmpty List<PurchaseOrderItemRequest> items,
        String notes) {
}
