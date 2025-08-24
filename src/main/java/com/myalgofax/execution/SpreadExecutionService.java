package com.myalgofax.execution;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class SpreadExecutionService {
    
    private final Map<String, SpreadExecution> activeExecutions = new ConcurrentHashMap<>();
    
    public static class SpreadLeg {
        private final String symbol;
        private final int quantity;
        private final String action;
        private final double theoreticalPrice;
        private volatile double fillPrice;
        private volatile boolean filled;
        
        public SpreadLeg(String symbol, int quantity, String action, double theoreticalPrice) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.action = action;
            this.theoreticalPrice = theoreticalPrice;
        }
        
        public String getSymbol() { return symbol; }
        public int getQuantity() { return quantity; }
        public String getAction() { return action; }
        public double getTheoreticalPrice() { return theoreticalPrice; }
        public double getFillPrice() { return fillPrice; }
        public void setFillPrice(double fillPrice) { this.fillPrice = fillPrice; }
        public boolean isFilled() { return filled; }
        public void setFilled(boolean filled) { this.filled = filled; }
    }
    
    public static class SpreadExecution {
        private final String executionId;
        private final List<SpreadLeg> legs;
        private final double maxCredit;
        private final LocalDateTime startTime;
        private volatile SpreadExecutionStatus status;
        
        public SpreadExecution(String executionId, List<SpreadLeg> legs, double maxCredit) {
            this.executionId = executionId;
            this.legs = legs;
            this.maxCredit = maxCredit;
            this.startTime = LocalDateTime.now();
            this.status = SpreadExecutionStatus.PENDING;
        }
        
        public String getExecutionId() { return executionId; }
        public List<SpreadLeg> getLegs() { return legs; }
        public double getMaxCredit() { return maxCredit; }
        public LocalDateTime getStartTime() { return startTime; }
        public SpreadExecutionStatus getStatus() { return status; }
        public void setStatus(SpreadExecutionStatus status) { this.status = status; }
    }
    
    public enum SpreadExecutionStatus {
        PENDING, PARTIAL, COMPLETED, FAILED, HEDGING
    }
    
    public Mono<String> executeSpread(List<SpreadLeg> legs, double minCredit) {
        return Mono.fromCallable(() -> {
            String executionId = generateExecutionId();
            double theoreticalCredit = calculateTheoreticalCredit(legs);
            
            if (theoreticalCredit < minCredit) {
                throw new RuntimeException("Theoretical credit below minimum threshold");
            }
            
            SpreadExecution execution = new SpreadExecution(executionId, legs, theoreticalCredit);
            activeExecutions.put(executionId, execution);
            return execution;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(this::executeLegsReactively)
        .map(SpreadExecution::getExecutionId);
    }
    
    private Mono<SpreadExecution> executeLegsReactively(SpreadExecution execution) {
        return Flux.fromIterable(execution.getLegs())
            .flatMap(leg -> executeLegReactively(leg, execution), 1) // Sequential execution
            .then(Mono.just(execution))
            .doOnNext(exec -> exec.setStatus(SpreadExecutionStatus.COMPLETED))
            .onErrorResume(error -> handleExecutionFailureReactively(execution, error));
    }
    
    private Mono<Void> executeLegReactively(SpreadLeg leg, SpreadExecution execution) {
        return getMarketPriceReactively(leg.getSymbol())
            .flatMap(marketPrice -> {
                double limitPrice = calculateLimitPrice(marketPrice, leg.getTheoreticalPrice(), leg.getAction());
                return attemptFillReactively(leg, limitPrice, marketPrice);
            })
            .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofMillis(100)))
            .onErrorResume(error -> handleLegFailureReactively(leg, execution, error));
    }
    
    private Mono<Double> getMarketPriceReactively(String symbol) {
        return Mono.fromCallable(() -> 100.0 + Math.random() * 10.0)
            .subscribeOn(Schedulers.boundedElastic())
            .delayElement(Duration.ofMillis(10)); // Simulate network latency
    }
    
    private Mono<Void> attemptFillReactively(SpreadLeg leg, double limitPrice, double marketPrice) {
        return Mono.fromCallable(() -> {
            boolean filled = simulateFill(limitPrice, marketPrice, leg.getAction());
            if (filled) {
                leg.setFillPrice(limitPrice);
                leg.setFilled(true);
                return null;
            } else {
                throw new RuntimeException("Failed to fill leg: " + leg.getSymbol());
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
    
    private Mono<SpreadExecution> handleExecutionFailureReactively(SpreadExecution execution, Throwable error) {
        return Mono.fromRunnable(() -> execution.setStatus(SpreadExecutionStatus.HEDGING))
            .then(hedgePartialFillReactively(execution))
            .then(Mono.fromRunnable(() -> execution.setStatus(SpreadExecutionStatus.FAILED)))
            .thenReturn(execution);
    }
    
    private Mono<Void> handleLegFailureReactively(SpreadLeg failedLeg, SpreadExecution execution, Throwable error) {
        return Mono.fromRunnable(() -> execution.setStatus(SpreadExecutionStatus.HEDGING))
            .then(hedgePartialFillReactively(execution))
            .then();
    }
    
    private Mono<Void> hedgePartialFillReactively(SpreadExecution execution) {
        return Flux.fromIterable(execution.getLegs())
            .filter(SpreadLeg::isFilled)
            .flatMap(this::calculateHedgeReactively)
            .then();
    }
    
    private Mono<Void> calculateHedgeReactively(SpreadLeg filledLeg) {
        return Mono.fromRunnable(() -> {
            System.out.println("Hedging required for leg: " + filledLeg.getSymbol());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
    
    private double calculateTheoreticalCredit(List<SpreadLeg> legs) {
        return legs.stream()
            .mapToDouble(leg -> leg.getAction().equals("SELL") ? 
                leg.getTheoreticalPrice() : -leg.getTheoreticalPrice())
            .sum();
    }
    
    private double calculateLimitPrice(double marketPrice, double theoreticalPrice, String action) {
        double midPrice = (marketPrice + theoreticalPrice) / 2.0;
        
        if ("BUY".equals(action)) {
            return Math.min(midPrice, theoreticalPrice * 1.01);
        } else {
            return Math.max(midPrice, theoreticalPrice * 0.99);
        }
    }
    
    private boolean simulateFill(double limitPrice, double marketPrice, String action) {
        double priceRatio = limitPrice / marketPrice;
        
        if ("BUY".equals(action)) {
            return priceRatio <= 1.005;
        } else {
            return priceRatio >= 0.995;
        }
    }
    
    private String generateExecutionId() {
        return "SPREAD_" + System.currentTimeMillis();
    }
    
    public Mono<SpreadExecution> getExecutionStatus(String executionId) {
        return Mono.fromCallable(() -> activeExecutions.get(executionId))
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    public Flux<SpreadExecution> getAllActiveExecutions() {
        return Flux.fromIterable(activeExecutions.values())
            .subscribeOn(Schedulers.boundedElastic());
    }
}