package com.myalgofax.execution.monitoring;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class TCAService {
    
    private final Map<String, ExecutionMetrics> executionHistory = new ConcurrentHashMap<>();
    
    public static class ExecutionMetrics {
        private final String orderId;
        private final String brokerId;
        private final double arrivalPrice;
        private final double executionPrice;
        private final int quantity;
        private final double commission;
        private final LocalDateTime signalTime;
        private final LocalDateTime executionTime;
        private final String algorithm;
        
        public ExecutionMetrics(String orderId, String brokerId, double arrivalPrice, 
                              double executionPrice, int quantity, double commission,
                              LocalDateTime signalTime, LocalDateTime executionTime, String algorithm) {
            this.orderId = orderId;
            this.brokerId = brokerId;
            this.arrivalPrice = arrivalPrice;
            this.executionPrice = executionPrice;
            this.quantity = quantity;
            this.commission = commission;
            this.signalTime = signalTime;
            this.executionTime = executionTime;
            this.algorithm = algorithm;
        }
        
        public double getSlippage() {
            return Math.abs(executionPrice - arrivalPrice) / arrivalPrice;
        }
        
        public double getImplementationShortfall() {
            double marketImpact = getSlippage() * quantity * arrivalPrice;
            return marketImpact + commission;
        }
        
        public long getExecutionLatencyMs() {
            return java.time.Duration.between(signalTime, executionTime).toMillis();
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getBrokerId() { return brokerId; }
        public double getArrivalPrice() { return arrivalPrice; }
        public double getExecutionPrice() { return executionPrice; }
        public int getQuantity() { return quantity; }
        public double getCommission() { return commission; }
        public LocalDateTime getSignalTime() { return signalTime; }
        public LocalDateTime getExecutionTime() { return executionTime; }
        public String getAlgorithm() { return algorithm; }
    }
    
    public static class TCAReport {
        private final String brokerId;
        private final String algorithm;
        private final double avgSlippage;
        private final double avgImplementationShortfall;
        private final long avgLatencyMs;
        private final int totalOrders;
        private final LocalDateTime reportPeriodStart;
        private final LocalDateTime reportPeriodEnd;
        
        public TCAReport(String brokerId, String algorithm, double avgSlippage, 
                        double avgImplementationShortfall, long avgLatencyMs, int totalOrders,
                        LocalDateTime reportPeriodStart, LocalDateTime reportPeriodEnd) {
            this.brokerId = brokerId;
            this.algorithm = algorithm;
            this.avgSlippage = avgSlippage;
            this.avgImplementationShortfall = avgImplementationShortfall;
            this.avgLatencyMs = avgLatencyMs;
            this.totalOrders = totalOrders;
            this.reportPeriodStart = reportPeriodStart;
            this.reportPeriodEnd = reportPeriodEnd;
        }
        
        // Getters
        public String getBrokerId() { return brokerId; }
        public String getAlgorithm() { return algorithm; }
        public double getAvgSlippage() { return avgSlippage; }
        public double getAvgImplementationShortfall() { return avgImplementationShortfall; }
        public long getAvgLatencyMs() { return avgLatencyMs; }
        public int getTotalOrders() { return totalOrders; }
        public LocalDateTime getReportPeriodStart() { return reportPeriodStart; }
        public LocalDateTime getReportPeriodEnd() { return reportPeriodEnd; }
    }
    
    public Mono<Void> recordExecution(String orderId, String brokerId, double arrivalPrice, 
                                     double executionPrice, int quantity, double commission,
                                     LocalDateTime signalTime, LocalDateTime executionTime, 
                                     String algorithm) {
        return Mono.fromRunnable(() -> {
            ExecutionMetrics metrics = new ExecutionMetrics(orderId, brokerId, arrivalPrice, 
                executionPrice, quantity, commission, signalTime, executionTime, algorithm);
            executionHistory.put(orderId, metrics);
        });
    }
    
    public Mono<TCAReport> generateBrokerReport(String brokerId, LocalDateTime startTime, LocalDateTime endTime) {
        return Flux.fromIterable(executionHistory.values())
            .filter(metrics -> metrics.getBrokerId().equals(brokerId))
            .filter(metrics -> isWithinPeriod(metrics.getExecutionTime(), startTime, endTime))
            .collectList()
            .map(metrics -> calculateReport(brokerId, "ALL", metrics, startTime, endTime));
    }
    
    public Mono<TCAReport> generateAlgorithmReport(String algorithm, LocalDateTime startTime, LocalDateTime endTime) {
        return Flux.fromIterable(executionHistory.values())
            .filter(metrics -> metrics.getAlgorithm().equals(algorithm))
            .filter(metrics -> isWithinPeriod(metrics.getExecutionTime(), startTime, endTime))
            .collectList()
            .map(metrics -> calculateReport("ALL", algorithm, metrics, startTime, endTime));
    }
    
    public Flux<TCAReport> generateComprehensiveReport(LocalDateTime startTime, LocalDateTime endTime) {
        return Flux.fromIterable(executionHistory.values())
            .filter(metrics -> isWithinPeriod(metrics.getExecutionTime(), startTime, endTime))
            .groupBy(metrics -> metrics.getBrokerId() + "_" + metrics.getAlgorithm())
            .flatMap(group -> group.collectList()
                .map(metrics -> {
                    String[] parts = group.key().split("_");
                    return calculateReport(parts[0], parts[1], metrics, startTime, endTime);
                }));
    }
    
    private boolean isWithinPeriod(LocalDateTime executionTime, LocalDateTime start, LocalDateTime end) {
        return !executionTime.isBefore(start) && !executionTime.isAfter(end);
    }
    
    private TCAReport calculateReport(String brokerId, String algorithm, List<ExecutionMetrics> metrics, 
                                    LocalDateTime start, LocalDateTime end) {
        if (metrics.isEmpty()) {
            return new TCAReport(brokerId, algorithm, 0.0, 0.0, 0L, 0, start, end);
        }
        
        double avgSlippage = metrics.stream()
            .mapToDouble(ExecutionMetrics::getSlippage)
            .average()
            .orElse(0.0);
            
        double avgImplementationShortfall = metrics.stream()
            .mapToDouble(ExecutionMetrics::getImplementationShortfall)
            .average()
            .orElse(0.0);
            
        long avgLatency = (long) metrics.stream()
            .mapToLong(ExecutionMetrics::getExecutionLatencyMs)
            .average()
            .orElse(0.0);
        
        return new TCAReport(brokerId, algorithm, avgSlippage, avgImplementationShortfall, 
                           avgLatency, metrics.size(), start, end);
    }
    
    public Mono<List<String>> getBrokerRankings(LocalDateTime startTime, LocalDateTime endTime) {
        return generateComprehensiveReport(startTime, endTime)
            .collectList()
            .map(reports -> {
                List<String> rankings = new ArrayList<>();
                reports.stream()
                    .sorted((r1, r2) -> Double.compare(r1.getAvgImplementationShortfall(), r2.getAvgImplementationShortfall()))
                    .forEach(report -> rankings.add(report.getBrokerId()));
                return rankings;
            });
    }
}