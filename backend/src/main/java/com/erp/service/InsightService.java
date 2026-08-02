package com.erp.service;

import com.erp.dto.InsightResponse;
import com.erp.entity.BusinessInsight;
import com.erp.entity.Inventory;
import com.erp.entity.Invoice;
import com.erp.entity.Product;
import com.erp.enums.InsightCategory;
import com.erp.enums.Severity;
import com.erp.repository.BusinessInsightRepository;
import com.erp.repository.InventoryRepository;
import com.erp.repository.InvoiceRepository;
import com.erp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsightService {

    private final BusinessInsightRepository insightRepository;
    private final InventoryRepository inventoryRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    @Value("${xiroa.ai.provider}")
    private String aiProvider;

    @Value("${xiroa.ai.openai-api-key}")
    private String openAiKey;

    @Value("${xiroa.ai.openai-model}")
    private String openAiModel;

    public InsightService(BusinessInsightRepository insightRepository,
            InventoryRepository inventoryRepository,
            InvoiceRepository invoiceRepository,
            ProductRepository productRepository) {
        this.insightRepository = insightRepository;
        this.inventoryRepository = inventoryRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
    }

    public List<InsightResponse> getAll() {
        return insightRepository.findAllByOrderByGeneratedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InsightResponse> generateAndSave() {
        List<BusinessInsight> insights = generateRuleBasedInsights();
        insightRepository.deleteAll();
        insightRepository.saveAll(insights);
        return insights.stream().map(this::toResponse).toList();
    }

    /**
     * Rule-based AI insights. In production, swap this to call an LLM
     * via the AiProvider interface (OpenAI / Gemini) for natural-language
     * summaries.
     */
    public List<BusinessInsight> generateRuleBasedInsights() {
        List<BusinessInsight> insights = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // ---- Low stock alert ----
        List<Inventory> lowStock = inventoryRepository.findByQuantityOnHandLessThanEqual(Integer.MAX_VALUE).stream()
                .filter(i -> i.getQuantityOnHand() <= i.getProduct().getReorderLevel())
                .toList();
        if (!lowStock.isEmpty()) {
            String names = lowStock.stream()
                    .map(i -> i.getProduct().getName() + " (" + i.getQuantityOnHand() + ")")
                    .limit(5)
                    .collect(Collectors.joining(", "));
            insights.add(insight("Low stock alert", "The following products are at or below reorder level: " + names,
                    InsightCategory.INVENTORY, Severity.CRITICAL,
                    "Place purchase orders for these items to avoid stockouts."));
        }

        // ---- Slow movers ----
        LocalDate today = LocalDate.now();
        LocalDateTime sixMonthsAgo = today.minusMonths(6).atStartOfDay();
        List<Product> allProducts = productRepository.findByActiveTrue();
        List<String> slowMovers = new ArrayList<>();
        for (Product p : allProducts) {
            BigDecimal revenue = invoiceRepository.sumTotalBetween(sixMonthsAgo, now);
            if (revenue.compareTo(BigDecimal.ZERO) == 0) {
                slowMovers.add(p.getName());
            }
        }
        if (!slowMovers.isEmpty()) {
            insights.add(insight("Slow-moving products", "Products with no sales in last 6 months: " +
                    slowMovers.stream().limit(5).collect(Collectors.joining(", ")),
                    InsightCategory.INVENTORY, Severity.WARNING,
                    "Run a discount promotion or bundle these with best sellers."));
        }

        // ---- Outstanding receivables ----
        List<Invoice> outstanding = invoiceRepository.findOutstandingInvoices();
        if (!outstanding.isEmpty()) {
            BigDecimal totalOutstanding = outstanding.stream()
                    .map(Invoice::getBalanceDue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            insights.add(insight("Outstanding receivables",
                    totalOutstanding.intValue() + " invoices with total outstanding of Rs. " +
                            totalOutstanding,
                    InsightCategory.CASHFLOW, Severity.WARNING,
                    "Send payment reminders and follow up on overdue invoices."));
        }

        // ---- Sales trend ----
        LocalDateTime thisMonthStart = today.withDayOfMonth(1).atStartOfDay();
        BigDecimal thisMonth = invoiceRepository.sumTotalBetween(thisMonthStart, now);
        LocalDateTime lastMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        BigDecimal lastMonth = invoiceRepository.sumTotalBetween(lastMonthStart, thisMonthStart);
        if (lastMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = thisMonth.subtract(lastMonth)
                    .multiply(BigDecimal.valueOf(100)).divide(lastMonth, 1, java.math.RoundingMode.HALF_UP);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                insights.add(insight("Sales momentum", "Sales this month are up " + change + "% vs last month.",
                        InsightCategory.SALES, Severity.INFO,
                        "Continue current strategies; consider increasing inventory of top sellers."));
            } else {
                insights.add(insight("Sales slowdown", "Sales this month are down " + change.abs() + "% vs last month.",
                        InsightCategory.SALES, Severity.WARNING,
                        "Review pricing, run promotions, and check competitor activity."));
            }
        }

        // ---- Best selling products ----
        var topProducts = invoiceRepository.topCustomers(); // reuse to see if any sales exist
        List<Inventory> inventories = inventoryRepository.findAll();
        inventories.stream()
                .max(Comparator.comparing(
                        i -> i.getProduct().getSellingPrice().multiply(BigDecimal.valueOf(i.getQuantityOnHand()))))
                .ifPresent(maxInv -> insights.add(insight("Top inventory value",
                        maxInv.getProduct().getName() + " holds the highest stock value.",
                        InsightCategory.INVENTORY, Severity.INFO, "Monitor demand closely for this product.")));

        if (insights.isEmpty()) {
            insights.add(insight("Business health", "Your business metrics look stable. Keep tracking daily.",
                    InsightCategory.GENERAL, Severity.INFO, "Continue monitoring dashboard metrics."));
        }
        return insights;
    }

    private BusinessInsight insight(String title, String desc, InsightCategory cat, Severity sev, String action) {
        BusinessInsight bi = new BusinessInsight();
        bi.setTitle(title);
        bi.setDescription(desc);
        bi.setCategory(cat);
        bi.setSeverity(sev);
        bi.setRecommendedAction(action);
        bi.setGeneratedAt(LocalDateTime.now());
        return bi;
    }

    private InsightResponse toResponse(BusinessInsight bi) {
        return new InsightResponse(bi.getId(), bi.getTitle(), bi.getDescription(),
                bi.getCategory(), bi.getSeverity(), bi.getGeneratedAt(), bi.getRecommendedAction());
    }

    /**
     * Optional OpenAI integration for natural-language insights.
     * When OPENAI_API_KEY is configured and ai.provider=openai, this can be wired.
     */
    public String callOpenAi(String prompt) {
        if (openAiKey == null || openAiKey.isBlank()) {
            return "OpenAI API key not configured. Set OPENAI_API_KEY env var and xiroa.ai.provider=openai.";
        }
        try {
            RestTemplate rt = new RestTemplate();
            var body = java.util.Map.of(
                    "model", openAiModel,
                    "messages", List.of(java.util.Map.of("role", "user", "content", prompt)),
                    "max_tokens", 400);
            var headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(openAiKey);
            var entity = new org.springframework.http.HttpEntity<>(body, headers);
            var resp = rt.postForObject("https://api.openai.com/v1/chat/completions", entity, String.class);
            if (resp == null) {
                return "No response from OpenAI";
            }
            // Extract clean text from choices[0].message.content
            try {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(resp);
                com.fasterxml.jackson.databind.JsonNode content = root.path("choices").path(0).path("message")
                        .path("content");
                if (!content.isMissingNode() && !content.asText().isBlank()) {
                    return content.asText();
                }
                com.fasterxml.jackson.databind.JsonNode err = root.path("error");
                return err.isMissingNode() ? resp : "OpenAI API error: " + err.path("message").asText();
            } catch (Exception e) {
                return resp; // not JSON — return raw
            }
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "Unknown error" : e.getMessage();
            if (msg.contains("429") || msg.contains("quota") || msg.contains("billing")) {
                return "⚠️ AI assistant is configured, but your OpenAI account has exceeded its quota or needs billing setup. "
                        +
                        "Add credits at https://platform.openai.com/settings/billing, then try again. " +
                        "Meanwhile, use \"Generate AI Insights\" (rule-based) for instant analysis.";
            }
            if (msg.contains("401") || msg.contains("Unauthorized") || msg.contains("invalid api key")) {
                return "⚠️ OpenAI API key is invalid. Check your OPENAI_API_KEY.";
            }
            return "OpenAI call failed: " + msg;
        }
    }
}
