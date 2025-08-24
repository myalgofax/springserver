package com.myalgofax.ui.analytics;

import com.myalgofax.ui.dto.PerformanceSnapshotDTO;
import com.myalgofax.ui.websocket.DashboardWebSocketHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PerformanceAnalyticsService {
    
    private final Map<String, List<PerformanceSnapshotDTO>> performanceHistory = new ConcurrentHashMap<>();
    private final Map<String, Double> currentPnL = new ConcurrentHashMap<>();
    private final DashboardWebSocketHandler dashboardHandler;
    
    public PerformanceAnalyticsService(DashboardWebSocketHandler dashboardHandler) {
        this.dashboardHandler = dashboardHandler;
    }
    
    public Mono<Void> updateStrategyPnL(String strategyId, double pnl) {
        return Mono.fromRunnable(() -> {
            currentPnL.put(strategyId, pnl);
            
            PerformanceSnapshotDTO snapshot = new PerformanceSnapshotDTO(
                Instant.now(),
                calculateEquity(),
                pnl,
                Map.of(strategyId, pnl)
            );
            
            performanceHistory.computeIfAbsent(strategyId, k -> new ArrayList<>()).add(snapshot);
            dashboardHandler.broadcastPnLUpdate(Map.of(strategyId, pnl));
        });
    }
    
    public Flux<PerformanceSnapshotDTO> getStrategyPerformanceHistory(String strategyId, 
                                                                    LocalDateTime from, 
                                                                    LocalDateTime to) {
        return Flux.fromIterable(performanceHistory.getOrDefault(strategyId, Collections.emptyList()))
            .filter(snapshot -> from == null || snapshot.timestamp().isAfter(from.atZone(ZoneId.systemDefault()).toInstant()))
            .filter(snapshot -> to == null || snapshot.timestamp().isBefore(to.atZone(ZoneId.systemDefault()).toInstant()));
    }
    
    public Mono<Map<String, Object>> getPortfolioSummary() {
        return Mono.fromCallable(() -> {
            double totalPnL = currentPnL.values().stream().mapToDouble(Double::doubleValue).sum();
            double equity = calculateEquity();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalPnL", totalPnL);
            summary.put("equity", equity);
            summary.put("sharpeRatio", calculatePortfolioSharpeRatio());
            summary.put("maxDrawdown", calculateMaxDrawdown());
            summary.put("activeStrategies", currentPnL.size());
            
            return summary;
        });
    }
    
    public Mono<Map<String, Double>> getCurrentRiskExposure() {
        return Mono.fromCallable(() -> {
            Map<String, Double> exposure = new HashMap<>();
            exposure.put("totalDelta", calculateTotalDelta());
            exposure.put("totalGamma", calculateTotalGamma());
            exposure.put("totalTheta", calculateTotalTheta());
            exposure.put("totalVega", calculateTotalVega());
            exposure.put("portfolioVar", calculatePortfolioVaR());
            
            return exposure;
        });
    }
    
    public Mono<Map<String, Double>> calculateStrategyMetrics(String strategyId) {
        return Mono.fromCallable(() -> {
            List<PerformanceSnapshotDTO> history = performanceHistory.get(strategyId);
            if (history == null || history.isEmpty()) {
                return Collections.emptyMap();
            }
            
            Map<String, Double> metrics = new HashMap<>();
            metrics.put("totalReturn", calculateTotalReturn(history));
            metrics.put("sharpeRatio", calculateSharpeRatio(history));
            metrics.put("maxDrawdown", calculateStrategyMaxDrawdown(history));
            metrics.put("winRate", calculateWinRate(history));
            metrics.put("profitFactor", calculateProfitFactor(history));
            
            return metrics;
        });
    }
    
    private double calculateEquity() {
        return 100000.0 + currentPnL.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    private double calculatePortfolioSharpeRatio() {
        // Simplified calculation
        double totalReturn = currentPnL.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalReturn > 0 ? totalReturn / 1000.0 : 0.0; // Simplified volatility
    }
    
    private double calculateMaxDrawdown() {
        // Simplified calculation
        return Math.random() * 0.1; // 0-10% drawdown
    }
    
    private double calculateTotalDelta() {
        return currentPnL.size() * 0.5; // Simplified delta calculation
    }
    
    private double calculateTotalGamma() {
        return currentPnL.size() * 0.1; // Simplified gamma calculation
    }
    
    private double calculateTotalTheta() {
        return currentPnL.size() * -2.0; // Simplified theta calculation
    }
    
    private double calculateTotalVega() {
        return currentPnL.size() * 15.0; // Simplified vega calculation
    }
    
    private double calculatePortfolioVaR() {
        double totalValue = calculateEquity();
        return totalValue * 0.02; // 2% VaR
    }
    
    private double calculateTotalReturn(List<PerformanceSnapshotDTO> history) {
        if (history.isEmpty()) return 0.0;
        return history.get(history.size() - 1).dailyPnl() - history.get(0).dailyPnl();
    }
    
    private double calculateSharpeRatio(List<PerformanceSnapshotDTO> history) {
        if (history.size() < 2) return 0.0;
        
        double[] returns = history.stream()
            .mapToDouble(PerformanceSnapshotDTO::dailyPnl)
            .toArray();
        
        double mean = Arrays.stream(returns).average().orElse(0.0);
        double variance = Arrays.stream(returns)
            .map(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        return stdDev > 0 ? (mean - 0.03) / stdDev : 0.0; // 3% risk-free rate
    }
    
    private double calculateStrategyMaxDrawdown(List<PerformanceSnapshotDTO> history) {
        if (history.isEmpty()) return 0.0;
        
        double peak = 0;
        double maxDrawdown = 0;
        
        for (PerformanceSnapshotDTO snapshot : history) {
            peak = Math.max(peak, snapshot.equity());
            double drawdown = (peak - snapshot.equity()) / peak;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }
        
        return maxDrawdown;
    }
    
    private double calculateWinRate(List<PerformanceSnapshotDTO> history) {
        if (history.isEmpty()) return 0.0;
        
        long winningDays = history.stream()
            .mapToDouble(PerformanceSnapshotDTO::dailyPnl)
            .mapToLong(pnl -> pnl > 0 ? 1 : 0)
            .sum();
        
        return (double) winningDays / history.size();
    }
    
    private double calculateProfitFactor(List<PerformanceSnapshotDTO> history) {
        double grossProfit = history.stream()
            .mapToDouble(PerformanceSnapshotDTO::dailyPnl)
            .filter(pnl -> pnl > 0)
            .sum();
        
        double grossLoss = Math.abs(history.stream()
            .mapToDouble(PerformanceSnapshotDTO::dailyPnl)
            .filter(pnl -> pnl < 0)
            .sum());
        
        return grossLoss > 0 ? grossProfit / grossLoss : 0.0;
    }
}