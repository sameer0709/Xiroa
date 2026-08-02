package com.erp.controller;

import com.erp.dto.InsightResponse;
import com.erp.service.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping
    public ResponseEntity<List<InsightResponse>> getAll() {
        return ResponseEntity.ok(insightService.getAll());
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<InsightResponse>> generate() {
        return ResponseEntity.ok(insightService.generateAndSave());
    }

    @GetMapping("/ai")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<String> aiChat(
            @RequestParam(defaultValue = "Summarize my business health and suggest improvements based on inventory, sales and cashflow.") String prompt) {
        return ResponseEntity.ok(insightService.callOpenAi(prompt));
    }
}
