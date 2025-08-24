package com.myalgofax.meta.portfolio;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetaStrategyManager {
    
    private final Map<String, StrategyPerformance> strategyPerformances = new ConcurrentHashMap<>();
    private final Map<String, Double> currentAllocations = new ConcurrentHashMap<>();
    private final PortfolioOptimizer portfolioOptimizer;
    
    public MetaStrategyManager(PortfolioOptimizer portfolioOptimizer) {
        this.portfolioOptimizer = portfolioOptimizer;
    }
    
    public static class StrategyPerformance {
        private final String strategyId;
        private double totalReturn;
        private double volatility;
        private double sharpeRatio;
        private double maxDrawdown;
        private int totalTrades;
        private double winRate;
        private LocalDateTime lastUpdate;
        private final List<Double> dailyReturns = new ArrayList<>();
        
        public StrategyPerformance(String strategyId) {
            this.strategyId = strategyId;
            this.lastUpdate = LocalDateTime.now();
        }
        
        public void updatePerformance(double dailyReturn) {
            dailyReturns.add(dailyReturn);
            if (dailyReturns.size() > 252) { // Keep 1 year of data
                dailyReturns.remove(0);
            }
            
            calculateMetrics();
            lastUpdate = LocalDateTime.now();
        }
        
        private void calculateMetrics() {
            if (dailyReturns.isEmpty()) return;
            
            // Calculate total return
            totalReturn = dailyReturns.stream().mapToDouble(Double::doubleValue).sum();
            
            // Calculate volatility (annualized)
            double mean = dailyReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = dailyReturns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average().orElse(0.0);
            volatility = Math.sqrt(variance * 252); // Annualized
            
            // Calculate Sharpe ratio (assuming 3% risk-free rate)
            double annualizedReturn = mean * 252;
            sharpeRatio = volatility > 0 ? (annualizedReturn - 0.03) / volatility : 0.0;
            
            // Calculate max drawdown
            calculateMaxDrawdown();
        }
        
        private void calculateMaxDrawdown() {
            if (dailyReturns.isEmpty()) return;
            
            double peak = 0;
            double maxDD = 0;
            double cumReturn = 0;
            
            for (double dailyReturn : dailyReturns) {
                cumReturn += dailyReturn;
                peak = Math.max(peak, cumReturn);
                double drawdown = (peak - cumReturn) / (1 + peak);
                maxDD = Math.max(maxDD, drawdown);
            }
            
            maxDrawdown = maxDD;
        }
        
        // Getters
        public String getStrategyId() { return strategyId; }
        public double getTotalReturn() { return totalReturn; }
        public double getVolatility() { return volatility; }
        public double getSharpeRatio() { return sharpeRatio; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public int getTotalTrades() { return totalTrades; }
        public double getWinRate() { return winRate; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public List<Double> getDailyReturns() { return new ArrayList<>(dailyReturns); }
        
        public void setTotalTrades(int totalTrades) { this.totalTrades = totalTrades; }
        public void setWinRate(double winRate) { this.winRate = winRate; }
    }
    
    public Mono<Void> updateStrategyPerformance(String strategyId, double dailyReturn, int trades, double winRate) {
        return Mono.fromRunnable(() -> {
            StrategyPerformance performance = strategyPerformances.computeIfAbsent(strategyId, StrategyPerformance::new);
            performance.updatePerformance(dailyReturn);
            performance.setTotalTrades(trades);
            performance.setWinRate(winRate);
        });
    }
    
    public Mono<Map<String, Double>> rebalancePortfolio(double totalCapital) {
        return Flux.fromIterable(strategyPerformances.values())
            .filter(perf -> perf.getDailyReturns().size() >= 30) // Minimum 30 days of data
            .collectList()
            .flatMap(performances -> portfolioOptimizer.optimizeAllocations(performances, totalCapital))
            .doOnNext(newAllocations -> {
                currentAllocations.clear();
                currentAllocations.putAll(newAllocations);
            });
    }
    
    public Mono<List<String>> identifyUnderperformingStrategies(double minSharpeRatio, double maxDrawdownThreshold) {
        return Flux.fromIterable(strategyPerformances.values())
            .filter(perf -> perf.getSharpeRatio() < minSharpeRatio || perf.getMaxDrawdown() > maxDrawdownThreshold)
            .map(StrategyPerformance::getStrategyId)
            .collectList();
    }
    
    public Mono<List<String>> identifyTopPerformingStrategies(int topN) {
        return Flux.fromIterable(strategyPerformances.values())
            .sort((p1, p2) -> Double.compare(p2.getSharpeRatio(), p1.getSharpeRatio()))
            .take(topN)
            .map(StrategyPerformance::getStrategyId)
            .collectList();
    }
    
    public Mono<Double> calculatePortfolioSharpeRatio() {
        return Flux.fromIterable(strategyPerformances.entrySet())
            .filter(entry -> currentAllocations.containsKey(entry.getKey()))
            .map(entry -> {
                double allocation = currentAllocations.get(entry.getKey());
                StrategyPerformance perf = entry.getValue();
                return allocation * perf.getSharpeRatio();
            })
            .reduce(0.0, Double::sum);
    }
    
    public Mono<Boolean> shouldRebalance() {
        return Mono.fromCallable(() -> {
            // Check if any strategy has significantly deviated from expected performance
            for (Map.Entry<String, StrategyPerformance> entry : strategyPerformances.entrySet()) {
                StrategyPerformance perf = entry.getValue();
                
                // Rebalance if strategy performance has changed significantly in last 7 days
                if (Duration.between(perf.getLastUpdate(), LocalDateTime.now()).toDays() < 7) {
                    List<Double> recentReturns = perf.getDailyReturns();
                    if (recentReturns.size() >= 7) {
                        double recentAvg = recentReturns.subList(recentReturns.size() - 7, recentReturns.size())
                            .stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double overallAvg = recentReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        
                        if (Math.abs(recentAvg - overallAvg) > 0.02) { // 2% deviation threshold
                            return true;
                        }
                    }
                }
            }
            return false;
        });   }
    
    public Map<String, Double> getCurrentAllocations() {
        return new HashMap<>(currentAllocations);
    }
    
    public Map<String, StrategyPerformance> getStrategyPerformances() {
        return new HashMap<>(strategyPerformances);
    }
}