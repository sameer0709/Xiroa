package com.erp.dto;

import com.erp.enums.InsightCategory;
import com.erp.enums.Severity;

import java.time.LocalDateTime;

public record InsightResponse(
        Long id,
        String title,
        String description,
        InsightCategory category,
        Severity severity,
        LocalDateTime generatedAt,
        String recommendedAction) {
}
