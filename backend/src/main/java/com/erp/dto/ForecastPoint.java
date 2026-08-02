package com.erp.dto;

import java.math.BigDecimal;

public record ForecastPoint(
        String period,
        BigDecimal actual,
        BigDecimal forecast) {
}
