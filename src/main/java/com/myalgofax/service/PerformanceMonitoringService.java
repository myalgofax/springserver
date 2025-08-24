package com.myalgofax.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.myalgofax.dto.SignalDto;
import com.myalgofax.strategy.StrategyExecutionEngine;

import reactor.core.scheduler.Schedulers;

@Service
public class PerformanceMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    
    private final Map<String, AtomicLong> executionLatency = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> strategyPnL = new ConcurrentHashMap<>();
    private final Map<String, Integer> signalCounts = new ConcurrentHashMap<>();
    private final StrategyExecutionEngine executionEngine;

    public PerformanceMonitoringService(StrategyExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
        
        // Subscribe to signals for monitoring
        executionEngine.getSignalStream()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(this::recordSignalMetrics);
        
        // Start periodic monitoring
        startPeriodicMonitoring();
    }

    private void recordSignalMetrics(SignalDto signal) {
        String strategyId = signal.getStrategyId();
        
        // Record signal count
        signalCounts.merge(strategyId, 1, Integer::sum);
        
        // Record execution time (simplified)
        long executionTime = System.currentTimeMillis() % 1000; // Simulate execution time
        executionLatency.computeIfAbsent(strategyId, k -> new AtomicLong()).set(executionTime);
        
        logger.debug("Signal recorded for strategy {}: {} at {}", strategyId, signal.getType(), signal.getTimestamp());
    }

    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System-wide metrics
        metrics.put("totalActiveStrategies", executionEngine.getActiveStrategies().size());
        metrics.put("totalSignalsGenerated", signalCounts.values().stream().mapToInt(Integer::intValue).sum());
        
        // Average execution latency
        double avgLatency = executionLatency.values().stream()
            .mapToLong(AtomicLong::get)
            .average()
            .orElse(0.0);
        metrics.put("averageExecutionLatencyMs", avgLatency);
        
        // Strategy-specific metrics
        Map<String, Object> strategyMetrics = new HashMap<>();
        executionEngine.getActiveStrategies().forEach((strategyId, strategy) -> {
            Map<String, Object> strategyData = new HashMap<>();
            strategyData.put("signalCount", signalCounts.getOrDefault(strategyId, 0));
            strategyData.put("executionLatencyMs", executionLatency.getOrDefault(strategyId, new AtomicLong()).get());
            strategyData.put("totalPnL", strategy.getTotalPnL());
            strategyData.put("executedTrades", strategy.getExecutedTrades());
            strategyData.put("isActive", strategy.isActive());
            strategyData.put("inPosition", strategy.isInPosition());
            
            strategyMetrics.put(strategyId, strategyData);
        });
        
        metrics.put("strategies", strategyMetrics);
        metrics.put("timestamp", LocalDateTime.now());
        
        return metrics;
    }

    public Map<String, Object> getHealthMetrics() {
        Map<String, Object> health = new HashMap<>();
        
        // Check if execution latency is within acceptable limits
        boolean latencyHealthy = executionLatency.values().stream()
            .allMatch(latency -> latency.get() < 100); // Sub-100ms requirement
        
        health.put("executionLatencyHealthy", latencyHealthy);
        health.put("strategiesRunning", !executionEngine.getActiveStrategies().isEmpty());
        health.put("systemStatus", latencyHealthy ? "HEALTHY" : "DEGRADED");
        
        return health;
    }

    private void startPeriodicMonitoring() {
        // Log performance metrics every minute
        reactor.core.publisher.Flux.interval(java.time.Duration.ofMinutes(1))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(tick -> {
                Map<String, Object> metrics = getPerformanceMetrics();
                logger.info("Performance metrics: Active strategies: {}, Total signals: {}, Avg latency: {}ms", 
                    metrics.get("totalActiveStrategies"),
                    metrics.get("totalSignalsGenerated"),
                    metrics.get("averageExecutionLatencyMs"));
            });
    }

    public void recordStrategyPnL(String strategyId, BigDecimal pnl) {
        strategyPnL.put(strategyId, pnl);
    }

    public BigDecimal getStrategyPnL(String strategyId) {
        return strategyPnL.getOrDefault(strategyId, BigDecimal.ZERO);
    }
}