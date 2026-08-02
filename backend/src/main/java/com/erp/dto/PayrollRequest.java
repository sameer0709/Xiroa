package com.erp.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate payDate,
        String month,
        int year,
        BigDecimal basic,
        BigDecimal hra,
        BigDecimal allowances,
        BigDecimal deductions) {
}
