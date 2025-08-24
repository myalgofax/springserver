package com.myalgofax.ui.controller;

import com.myalgofax.ui.dto.PerformanceSnapshotDTO;
import com.myalgofax.ui.analytics.PerformanceAnalyticsService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    private final PerformanceAnalyticsService analyticsService;
    
    public MonitoringController(PerformanceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/strategy/{id}")
    public Flux<PerformanceSnapshotDTO> getStrategyPerformance(@PathVariable String id,
                                                             @RequestParam(required = false) LocalDateTime from,
                                                             @RequestParam(required = false) LocalDateTime to) {
        return analyticsService.getStrategyPerformanceHistory(id, from, to);
    }
    
    @GetMapping("/portfolio")
    public Mono<Map<String, Object>> getPortfolioPerformance() {
        return analyticsService.getPortfolioSummary();
    }
    
    @GetMapping("/risk/exposure")
    public Mono<Map<String, Double>> getRiskExposure() {
        return analyticsService.getCurrentRiskExposure();
    }
    
    @GetMapping("/metrics/{strategyId}")
    public Mono<Map<String, Double>> getStrategyMetrics(@PathVariable String strategyId) {
        return analyticsService.calculateStrategyMetrics(strategyId);
    }
}