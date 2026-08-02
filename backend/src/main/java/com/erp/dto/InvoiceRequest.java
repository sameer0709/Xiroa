package com.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceRequest(
        @NotNull Long customerId,
        @NotEmpty List<InvoiceItemRequest> items,
        BigDecimal amountPaid,
        String paymentMode,
        String notes) {
}
