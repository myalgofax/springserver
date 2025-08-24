package com.myalgofax.ui.controller;

import com.myalgofax.ui.analytics.PerformanceAnalyticsService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {
    
    private final PerformanceAnalyticsService analyticsService;
    
    public RiskController(PerformanceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/exposure")
    public Mono<Map<String, Double>> getCurrentRiskExposure() {
        return analyticsService.getCurrentRiskExposure();
    }
    
    @GetMapping("/var")
    public Mono<Map<String, Object>> getVaRMetrics() {
        return Mono.just(Map.of(
            "dailyVaR", 2500.0,
            "weeklyVaR", 5500.0,
            "confidence", 95.0,
            "portfolioValue", 100000.0
        ));
    }
    
    @GetMapping("/limits")
    public Mono<Map<String, Object>> getRiskLimits() {
        return Mono.just(Map.of(
            "maxPortfolioVaR", 10000.0,
            "maxSingleStrategyRisk", 5000.0,
            "maxDrawdown", 0.15,
            "currentUtilization", 0.65
        ));
    }
    
    @PutMapping("/limits")
    public Mono<Void> updateRiskLimits(@RequestBody Map<String, Object> limits) {
        return Mono.empty();
    }
}