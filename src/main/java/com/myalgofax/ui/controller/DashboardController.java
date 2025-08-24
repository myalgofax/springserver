package com.myalgofax.ui.controller;

import com.myalgofax.ui.dto.*;
import com.myalgofax.ui.analytics.PerformanceAnalyticsService;
import com.myalgofax.ui.alerts.AlertService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final PerformanceAnalyticsService analyticsService;
    private final AlertService alertService;
    
    public DashboardController(PerformanceAnalyticsService analyticsService, AlertService alertService) {
        this.analyticsService = analyticsService;
        this.alertService = alertService;
    }
    
    @GetMapping("/overview")
    public Mono<Map<String, Object>> getDashboardOverview() {
        return analyticsService.getPortfolioSummary();
    }
    
    @GetMapping("/real-time-metrics")
    public Flux<Map<String, Object>> getRealTimeMetrics() {
        return Flux.interval(java.time.Duration.ofSeconds(1))
            .flatMap(tick -> analyticsService.getPortfolioSummary());
    }
    
    @GetMapping("/alerts")
    public Flux<AlertService.Alert> getActiveAlerts() {
        return Flux.just(
            new AlertService.Alert("RISK_ALERT", "Portfolio VaR exceeded", "HIGH", Map.of()),
            new AlertService.Alert("STRATEGY_ALERT", "Iron Condor underperforming", "MEDIUM", Map.of())
        );
    }
    
    @PostMapping("/alerts/acknowledge/{alertId}")
    public Mono<Void> acknowledgeAlert(@PathVariable String alertId) {
        return Mono.empty();
    }
}