package com.erp.dto;

import java.math.BigDecimal;
import java.util.List;

public record ForecastResponse(
        String method,
        List<ForecastPoint> points,
        BigDecimal nextPeriodForecast,
        BigDecimal confidence) {
}
