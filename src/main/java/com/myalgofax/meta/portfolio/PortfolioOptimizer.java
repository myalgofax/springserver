package com.myalgofax.meta.portfolio;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.*;

@Component
public class PortfolioOptimizer {
    
    private static final double RISK_FREE_RATE = 0.03;
    private static final double MIN_ALLOCATION = 0.05;
    private static final double MAX_ALLOCATION = 0.30;
    
    public Mono<Map<String, Double>> optimizeAllocations(List<MetaStrategyManager.StrategyPerformance> performances, double totalCapital) {
        return Mono.fromCallable(() -> {
            if (performances.isEmpty()) {
                return Collections.emptyMap();
            }
            
            return optimizeUsingMPT(performances, totalCapital);
        });
    }
    
    private Map<String, Double> optimizeUsingMPT(List<MetaStrategyManager.StrategyPerformance> performances, double totalCapital) {
        int n = performances.size();
        
        double[] expectedReturns = new double[n];
        double[][] covarianceMatrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            MetaStrategyManager.StrategyPerformance perf = performances.get(i);
            expectedReturns[i] = perf.getDailyReturns().stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0) * 252;
        }
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                covarianceMatrix[i][j] = calculateCovariance(
                    performances.get(i).getDailyReturns(),
                    performances.get(j).getDailyReturns()
                ) * 252;
            }
        }
        
        double[] optimalWeights = optimizeForMaxSharpe(expectedReturns, covarianceMatrix);
        
        Map<String, Double> allocations = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String strategyId = performances.get(i).getStrategyId();
            double allocation = Math.max(MIN_ALLOCATION, Math.min(MAX_ALLOCATION, optimalWeights[i]));
            allocations.put(strategyId, allocation);
        }
        
        normalizeAllocations(allocations);
        
        return allocations;
    }
    
    private double calculateCovariance(List<Double> returns1, List<Double> returns2) {
        if (returns1.size() != returns2.size() || returns1.isEmpty()) {
            return 0.0;
        }
        
        double mean1 = returns1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mean2 = returns2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double covariance = 0.0;
        for (int i = 0; i < returns1.size(); i++) {
            covariance += (returns1.get(i) - mean1) * (returns2.get(i) - mean2);
        }
        
        return covariance / (returns1.size() - 1);
    }
    
    private double[] optimizeForMaxSharpe(double[] expectedReturns, double[][] covarianceMatrix) {
        int n = expectedReturns.length;
        
        double[] weights = new double[n];
        double[] scores = new double[n];
        double totalScore = 0.0;
        
        for (int i = 0; i < n; i++) {
            double excessReturn = expectedReturns[i] - RISK_FREE_RATE;
            double volatility = Math.sqrt(covarianceMatrix[i][i]);
            scores[i] = volatility > 0 ? excessReturn / volatility : 0.0;
            scores[i] = Math.max(0, scores[i]);
            totalScore += scores[i];
        }
        
        if (totalScore > 0) {
            for (int i = 0; i < n; i++) {
                weights[i] = scores[i] / totalScore;
            }
        } else {
            Arrays.fill(weights, 1.0 / n);
        }
        
        return applyConstraintsAndImprove(weights, expectedReturns, covarianceMatrix);
    }
    
    private double[] applyConstraintsAndImprove(double[] initialWeights, double[] expectedReturns, double[][] covarianceMatrix) {
        double[] weights = initialWeights.clone();
        
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.max(MIN_ALLOCATION, Math.min(MAX_ALLOCATION, weights[i]));
        }
        
        double sum = Arrays.stream(weights).sum();
        if (sum > 0) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= sum;
            }
        }
        
        for (int iter = 0; iter < 10; iter++) {
            double[] gradient = calculateSharpeGradient(weights, expectedReturns, covarianceMatrix);
            
            double stepSize = 0.01;
            for (int i = 0; i < weights.length; i++) {
                weights[i] += stepSize * gradient[i];
                weights[i] = Math.max(MIN_ALLOCATION, Math.min(MAX_ALLOCATION, weights[i]));
            }
            
            sum = Arrays.stream(weights).sum();
            if (sum > 0) {
                for (int i = 0; i < weights.length; i++) {
                    weights[i] /= sum;
                }
            }
        }
        
        return weights;
    }
    
    private double[] calculateSharpeGradient(double[] weights, double[] expectedReturns, double[][] covarianceMatrix) {
        int n = weights.length;
        double[] gradient = new double[n];
        
        double portfolioReturn = 0.0;
        for (int i = 0; i < n; i++) {
            portfolioReturn += weights[i] * expectedReturns[i];
        }
        
        double portfolioVariance = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                portfolioVariance += weights[i] * weights[j] * covarianceMatrix[i][j];
            }
        }
        
        double portfolioStdDev = Math.sqrt(portfolioVariance);
        double excessReturn = portfolioReturn - RISK_FREE_RATE;
        
        if (portfolioStdDev > 0) {
            for (int i = 0; i < n; i++) {
                double returnGradient = expectedReturns[i] - RISK_FREE_RATE;
                
                double varianceGradient = 0.0;
                for (int j = 0; j < n; j++) {
                    varianceGradient += 2 * weights[j] * covarianceMatrix[i][j];
                }
                
                double stdDevGradient = varianceGradient / (2 * portfolioStdDev);
                
                gradient[i] = (returnGradient * portfolioStdDev - excessReturn * stdDevGradient) / (portfolioVariance);
            }
        }
        
        return gradient;
    }
    
    private void normalizeAllocations(Map<String, Double> allocations) {
        double sum = allocations.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            allocations.replaceAll((k, v) -> v / sum);
        }
    }
}