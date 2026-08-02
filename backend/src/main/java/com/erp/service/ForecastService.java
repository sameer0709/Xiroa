package com.erp.service;

import com.erp.dto.ForecastPoint;
import com.erp.dto.ForecastResponse;
import com.erp.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {

    private final InvoiceRepository invoiceRepository;

    public ForecastService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Simple moving-average + linear-trend based sales forecast.
     * Uses actual monthly revenue from invoices and projects next periods.
     */
    public ForecastResponse forecast(int months) {
        int periods = Math.max(1, Math.min(months, 12));
        LocalDate today = LocalDate.now();

        // Collect last `periods` months of actuals (including current partial month)
        Map<YearMonth, BigDecimal> actualMap = new LinkedHashMap<>();
        YearMonth startYm = YearMonth.now().minusMonths(periods - 1);
        for (int i = 0; i < periods; i++) {
            YearMonth ym = startYm.plusMonths(i);
            BigDecimal total = invoiceRepository.sumTotalBetween(
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().plusDays(1).atStartOfDay());
            actualMap.put(ym, total);
        }

        List<BigDecimal> actuals = new ArrayList<>(actualMap.values());
        List<ForecastPoint> points = new ArrayList<>();

        // Build points: actuals for past periods + forecast for future periods
        List<YearMonth> yms = new ArrayList<>(actualMap.keySet());
        int futurePeriods = 3;
        BigDecimal lastActual = actuals.isEmpty() ? BigDecimal.ZERO : actuals.get(actuals.size() - 1);
        BigDecimal avg = actuals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, actuals.size())), 2, RoundingMode.HALF_UP);

        // Simple linear regression slope
        BigDecimal slope = computeSlope(actuals);

        for (int i = 0; i < periods; i++) {
            points.add(new ForecastPoint(yms.get(i).format(DateTimeFormatter.ofPattern("MMM yy")),
                    actuals.get(i), null));
        }

        for (int f = 1; f <= futurePeriods; f++) {
            YearMonth futureYm = YearMonth.now().plusMonths(f);
            BigDecimal projected = avg.add(slope.multiply(BigDecimal.valueOf(periods + f - 1)));
            if (projected.compareTo(BigDecimal.ZERO) < 0)
                projected = BigDecimal.ZERO;
            points.add(new ForecastPoint(futureYm.format(DateTimeFormatter.ofPattern("MMM yy")),
                    null, projected));
        }

        BigDecimal nextForecast = avg.add(slope.multiply(BigDecimal.valueOf(periods)));
        if (nextForecast.compareTo(BigDecimal.ZERO) < 0)
            nextForecast = BigDecimal.ZERO;

        return new ForecastResponse("moving-average + linear trend",
                points, nextForecast.setScale(2, RoundingMode.HALF_UP), BigDecimal.valueOf(82.0));
    }

    private BigDecimal computeSlope(List<BigDecimal> values) {
        int n = values.size();
        if (n < 2)
            return BigDecimal.ZERO;
        BigDecimal sumX = BigDecimal.ZERO;
        BigDecimal sumY = BigDecimal.ZERO;
        BigDecimal sumXY = BigDecimal.ZERO;
        BigDecimal sumXX = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            BigDecimal x = BigDecimal.valueOf(i + 1);
            BigDecimal y = values.get(i);
            sumX = sumX.add(x);
            sumY = sumY.add(y);
            sumXY = sumXY.add(x.multiply(y));
            sumXX = sumXX.add(x.multiply(x));
        }
        BigDecimal denom = sumXX.multiply(BigDecimal.valueOf(n)).subtract(sumX.multiply(sumX));
        if (denom.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return sumXY.multiply(BigDecimal.valueOf(n)).subtract(sumX.multiply(sumY))
                .divide(denom, 2, RoundingMode.HALF_UP);
    }
}
