package com.myalgofax.execution.monitoring;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class LatencyMonitoringService {
    
    private final Map<String, List<LatencyMeasurement>> latencyHistory = new ConcurrentHashMap<>();
    private final Map<String, Long> activeTimestamps = new ConcurrentHashMap<>();
    
    public static class LatencyMeasurement {
        private final String stage;
        private final long latencyMs;
        private final LocalDateTime timestamp;
        private final String orderId;
        
        public LatencyMeasurement(String stage, long latencyMs, LocalDateTime timestamp, String orderId) {
            this.stage = stage;
            this.latencyMs = latencyMs;
            this.timestamp = timestamp;
            this.orderId = orderId;
        }
        
        public String getStage() { return stage; }
        public long getLatencyMs() { return latencyMs; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getOrderId() { return orderId; }
    }
    
    public static class LatencyStats {
        private final String stage;
        private final double avgLatencyMs;
        private final long p50LatencyMs;
        private final long p95LatencyMs;
        private final long p99LatencyMs;
        private final long maxLatencyMs;
        private final int sampleCount;
        
        public LatencyStats(String stage, double avgLatencyMs, long p50LatencyMs, 
                           long p95LatencyMs, long p99LatencyMs, long maxLatencyMs, int sampleCount) {
            this.stage = stage;
            this.avgLatencyMs = avgLatencyMs;
            this.p50LatencyMs = p50LatencyMs;
            this.p95LatencyMs = p95LatencyMs;
            this.p99LatencyMs = p99LatencyMs;
            this.maxLatencyMs = maxLatencyMs;
            this.sampleCount = sampleCount;
        }
        
        // Getters
        public String getStage() { return stage; }
        public double getAvgLatencyMs() { return avgLatencyMs; }
        public long getP50LatencyMs() { return p50LatencyMs; }
        public long getP95LatencyMs() { return p95LatencyMs; }
        public long getP99LatencyMs() { return p99LatencyMs; }
        public long getMaxLatencyMs() { return maxLatencyMs; }
        public int getSampleCount() { return sampleCount; }
    }
    
    public enum TradingStage {
        SIGNAL_GENERATION("SIGNAL_GENERATION"),
        ORDER_ROUTING("ORDER_ROUTING"),
        ORDER_SENT("ORDER_SENT"),
        ORDER_ACK("ORDER_ACK"),
        ORDER_FILL("ORDER_FILL");
        
        private final String stageName;
        
        TradingStage(String stageName) {
            this.stageName = stageName;
        }
        
        public String getStageName() {
            return stageName;
        }
    }
    
    public Mono<Void> startLatencyTracking(String orderId) {
        return Mono.fromRunnable(() -> {
            activeTimestamps.put(orderId, System.currentTimeMillis());
        });
    }
    
    public Mono<Void> recordStageLatency(String orderId, TradingStage stage) {
        return Mono.fromCallable(() -> {
            Long startTime = activeTimestamps.get(orderId);
            if (startTime == null) {
                throw new RuntimeException("No start timestamp found for order: " + orderId);
            }
            
            long currentTime = System.currentTimeMillis();
            long latency = currentTime - startTime;
            
            LatencyMeasurement measurement = new LatencyMeasurement(
                stage.getStageName(), latency, LocalDateTime.now(), orderId);
            
            latencyHistory.computeIfAbsent(stage.getStageName(), k -> new ArrayList<>())
                         .add(measurement);
            
            // Update timestamp for next stage
            activeTimestamps.put(orderId, currentTime);
            
            return null;
        }).then();
    }
    
    public Mono<Void> completeLatencyTracking(String orderId) {
        return Mono.fromRunnable(() -> {
            activeTimestamps.remove(orderId);
        });
    }
    
    public Mono<LatencyStats> getLatencyStats(TradingStage stage, Duration lookbackPeriod) {
        return Flux.fromIterable(latencyHistory.getOrDefault(stage.getStageName(), Collections.emptyList()))
            .filter(measurement -> isWithinLookback(measurement.getTimestamp(), lookbackPeriod))
            .map(LatencyMeasurement::getLatencyMs)
            .collectList()
            .map(latencies -> calculateStats(stage.getStageName(), latencies));
    }
    
    public Flux<LatencyStats> getAllStageStats(Duration lookbackPeriod) {
        return Flux.fromArray(TradingStage.values())
            .flatMap(stage -> getLatencyStats(stage, lookbackPeriod));
    }
    
    public Mono<Boolean> checkLatencyAlert(TradingStage stage, long thresholdMs) {
        return getLatencyStats(stage, Duration.ofMinutes(5))
            .map(stats -> stats.getP99LatencyMs() > thresholdMs);
    }
    
    public Flux<String> getLatencyAlerts(Duration lookbackPeriod) {
        Map<TradingStage, Long> thresholds = Map.of(
            TradingStage.SIGNAL_GENERATION, 50L,
            TradingStage.ORDER_ROUTING, 10L,
            TradingStage.ORDER_SENT, 100L,
            TradingStage.ORDER_ACK, 200L,
            TradingStage.ORDER_FILL, 1000L
        );
        
        return Flux.fromIterable(thresholds.entrySet())
            .flatMap(entry -> checkLatencyAlert(entry.getKey(), entry.getValue())
                .filter(isAlert -> isAlert)
                .map(isAlert -> String.format("ALERT: %s latency exceeded %dms threshold", 
                    entry.getKey().getStageName(), entry.getValue())));
    }
    
    private boolean isWithinLookback(LocalDateTime timestamp, Duration lookback) {
        return timestamp.isAfter(LocalDateTime.now().minus(lookback));
    }
    
    private LatencyStats calculateStats(String stage, List<Long> latencies) {
        if (latencies.isEmpty()) {
            return new LatencyStats(stage, 0.0, 0L, 0L, 0L, 0L, 0);
        }
        
        Collections.sort(latencies);
        
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long p50 = getPercentile(latencies, 0.50);
        long p95 = getPercentile(latencies, 0.95);
        long p99 = getPercentile(latencies, 0.99);
        long max = latencies.get(latencies.size() - 1);
        
        return new LatencyStats(stage, avg, p50, p95, p99, max, latencies.size());
    }
    
    private long getPercentile(List<Long> sortedLatencies, double percentile) {
        if (sortedLatencies.isEmpty()) return 0L;
        
        int index = (int) Math.ceil(percentile * sortedLatencies.size()) - 1;
        index = Math.max(0, Math.min(index, sortedLatencies.size() - 1));
        
        return sortedLatencies.get(index);
    }
    
    public Mono<Void> injectHeartbeat(String orderId, TradingStage stage) {
        return Mono.fromRunnable(() -> {
            // Inject timestamped heartbeat for monitoring
            System.out.println(String.format("HEARTBEAT: Order %s reached stage %s at %s", 
                orderId, stage.getStageName(), LocalDateTime.now()));
        });
    }
}