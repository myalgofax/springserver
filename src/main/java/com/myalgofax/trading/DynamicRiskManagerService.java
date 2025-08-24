package com.myalgofax.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.myalgofax.trading.MLPredictionService.MLPredictionResult;

import reactor.core.publisher.Mono;

/**
 * Dynamic Risk Manager implementing Kelly Criterion for position sizing
 * Integrates with ML predictions for adaptive risk management
 */
@Service
public class DynamicRiskManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicRiskManagerService.class);
    
    // Portfolio-level risk limits
    @Value("${risk.max.portfolio.delta:1000}")
    private BigDecimal maxPortfolioDelta;
    
    @Value("${risk.max.portfolio.vega:500}")
    private BigDecimal maxPortfolioVega;
    
    @Value("${risk.max.single.position:50000}")
    private BigDecimal maxSinglePosition;
    
    @Value("${risk.kelly.fraction:0.25}")
    private double kellyFraction; // Fractional Kelly to reduce risk
    
    // Current portfolio risk metrics
    private final Map<String, BigDecimal> userPortfolioDelta = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> userPortfolioVega = new ConcurrentHashMap<>();

    /**
     * Calculate optimal position size using Kelly Criterion with ML prediction
     * Formula: f* = (p * b - (1 - p)) / b * kellyFraction
     * Final size: min(staticMaxCapital, kellySizedCapital, riskLimitSizedCapital)
     */
    public Mono<PositionSizeResult> calculatePositionSize(PositionSizeRequest request) {
        return Mono.fromCallable(() -> {
            // Extract parameters
            double probabilityOfProfit = request.getMlPrediction().getProbabilityOfProfit();
            double odds = request.getMaxProfit().divide(request.getMaxLoss(), 4, RoundingMode.HALF_UP).doubleValue();
            BigDecimal availableCapital = request.getAvailableCapital();
            String userId = request.getUserId();
            
            // Kelly Criterion calculation
            double kellyF = ((probabilityOfProfit * odds) - (1 - probabilityOfProfit)) / odds;
            kellyF = Math.max(0, kellyF * kellyFraction); // Apply fractional Kelly and ensure non-negative
            
            BigDecimal kellySizedCapital = availableCapital.multiply(BigDecimal.valueOf(kellyF));
            
            // Apply static maximum limit
            BigDecimal staticLimitedCapital = kellySizedCapital.min(maxSinglePosition);
            
            // Apply portfolio-level risk limits
            BigDecimal riskLimitedCapital = applyPortfolioRiskLimits(
                staticLimitedCapital, request, userId);
            
            // Final position size
            BigDecimal finalPositionSize = riskLimitedCapital;
            
            logger.info("Position sizing for user {}: Kelly={}, Static={}, RiskLimited={}, Final={}", 
                userId, kellySizedCapital, staticLimitedCapital, riskLimitedCapital, finalPositionSize);
            
            return new PositionSizeResult(
                finalPositionSize,
                kellyF,
                kellySizedCapital,
                staticLimitedCapital,
                riskLimitedCapital,
                calculateLotSize(finalPositionSize, request.getLotSize())
            );
        });
    }

    /**
     * Apply portfolio-level risk limits (Delta, Vega, etc.)
     */
    private BigDecimal applyPortfolioRiskLimits(BigDecimal proposedSize, PositionSizeRequest request, String userId) {
        BigDecimal currentDelta = userPortfolioDelta.getOrDefault(userId, BigDecimal.ZERO);
        BigDecimal currentVega = userPortfolioVega.getOrDefault(userId, BigDecimal.ZERO);
        
        // Calculate position Greeks
        BigDecimal positionDelta = proposedSize.multiply(request.getDeltaPerUnit());
        BigDecimal positionVega = proposedSize.multiply(request.getVegaPerUnit());
        
        // Check Delta limit
        BigDecimal newDelta = currentDelta.add(positionDelta);
        if (newDelta.abs().compareTo(maxPortfolioDelta) > 0) {
            BigDecimal deltaReduction = newDelta.abs().subtract(maxPortfolioDelta);
            BigDecimal sizeReduction = deltaReduction.divide(request.getDeltaPerUnit().abs(), 2, RoundingMode.HALF_UP);
            proposedSize = proposedSize.subtract(sizeReduction).max(BigDecimal.ZERO);
            logger.warn("Position size reduced due to Delta limit for user {}: {}", userId, proposedSize);
        }
        
        // Check Vega limit
        BigDecimal newVega = currentVega.add(positionVega);
        if (newVega.abs().compareTo(maxPortfolioVega) > 0) {
            BigDecimal vegaReduction = newVega.abs().subtract(maxPortfolioVega);
            BigDecimal sizeReduction = vegaReduction.divide(request.getVegaPerUnit().abs(), 2, RoundingMode.HALF_UP);
            proposedSize = proposedSize.subtract(sizeReduction).max(BigDecimal.ZERO);
            logger.warn("Position size reduced due to Vega limit for user {}: {}", userId, proposedSize);
        }
        
        return proposedSize;
    }

    /**
     * Convert position size to lot size for options trading
     */
    private int calculateLotSize(BigDecimal positionSize, int standardLotSize) {
        if (positionSize.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        // Calculate number of lots (minimum 1 if position size > 0)
        int lots = positionSize.divide(BigDecimal.valueOf(standardLotSize), 0, RoundingMode.DOWN).intValue();
        return Math.max(1, lots);
    }

    /**
     * Update portfolio risk metrics after position execution
     */
    public void updatePortfolioRisk(String userId, BigDecimal deltaChange, BigDecimal vegaChange) {
        userPortfolioDelta.merge(userId, deltaChange, BigDecimal::add);
        userPortfolioVega.merge(userId, vegaChange, BigDecimal::add);
        
        logger.debug("Updated portfolio risk for user {}: Delta={}, Vega={}", 
            userId, userPortfolioDelta.get(userId), userPortfolioVega.get(userId));
    }

    /**
     * Get current portfolio risk metrics
     */
    public PortfolioRiskMetrics getPortfolioRisk(String userId) {
        return new PortfolioRiskMetrics(
            userPortfolioDelta.getOrDefault(userId, BigDecimal.ZERO),
            userPortfolioVega.getOrDefault(userId, BigDecimal.ZERO),
            maxPortfolioDelta,
            maxPortfolioVega
        );
    }

    /**
     * Position Size Request DTO
     */
    public static class PositionSizeRequest {
        private final MLPredictionResult mlPrediction;
        private final BigDecimal maxProfit;
        private final BigDecimal maxLoss;
        private final BigDecimal availableCapital;
        private final String userId;
        private final BigDecimal deltaPerUnit;
        private final BigDecimal vegaPerUnit;
        private final int lotSize;

        public PositionSizeRequest(MLPredictionResult mlPrediction, BigDecimal maxProfit, BigDecimal maxLoss,
                BigDecimal availableCapital, String userId, BigDecimal deltaPerUnit, BigDecimal vegaPerUnit, int lotSize) {
            this.mlPrediction = mlPrediction;
            this.maxProfit = maxProfit;
            this.maxLoss = maxLoss;
            this.availableCapital = availableCapital;
            this.userId = userId;
            this.deltaPerUnit = deltaPerUnit;
            this.vegaPerUnit = vegaPerUnit;
            this.lotSize = lotSize;
        }

        public MLPredictionResult getMlPrediction() { return mlPrediction; }
        public BigDecimal getMaxProfit() { return maxProfit; }
        public BigDecimal getMaxLoss() { return maxLoss; }
        public BigDecimal getAvailableCapital() { return availableCapital; }
        public String getUserId() { return userId; }
        public BigDecimal getDeltaPerUnit() { return deltaPerUnit; }
        public BigDecimal getVegaPerUnit() { return vegaPerUnit; }
        public int getLotSize() { return lotSize; }
    }

    /**
     * Position Size Result
     */
    public static class PositionSizeResult {
        private final BigDecimal finalPositionSize;
        private final double kellyFraction;
        private final BigDecimal kellySizedCapital;
        private final BigDecimal staticLimitedCapital;
        private final BigDecimal riskLimitedCapital;
        private final int lotSize;

        public PositionSizeResult(BigDecimal finalPositionSize, double kellyFraction, BigDecimal kellySizedCapital,
                BigDecimal staticLimitedCapital, BigDecimal riskLimitedCapital, int lotSize) {
            this.finalPositionSize = finalPositionSize;
            this.kellyFraction = kellyFraction;
            this.kellySizedCapital = kellySizedCapital;
            this.staticLimitedCapital = staticLimitedCapital;
            this.riskLimitedCapital = riskLimitedCapital;
            this.lotSize = lotSize;
        }

        public BigDecimal getFinalPositionSize() { return finalPositionSize; }
        public double getKellyFraction() { return kellyFraction; }
        public BigDecimal getKellySizedCapital() { return kellySizedCapital; }
        public BigDecimal getStaticLimitedCapital() { return staticLimitedCapital; }
        public BigDecimal getRiskLimitedCapital() { return riskLimitedCapital; }
        public int getLotSize() { return lotSize; }
    }

    /**
     * Portfolio Risk Metrics
     */
    public static class PortfolioRiskMetrics {
        private final BigDecimal currentDelta;
        private final BigDecimal currentVega;
        private final BigDecimal maxDelta;
        private final BigDecimal maxVega;

        public PortfolioRiskMetrics(BigDecimal currentDelta, BigDecimal currentVega, BigDecimal maxDelta, BigDecimal maxVega) {
            this.currentDelta = currentDelta;
            this.currentVega = currentVega;
            this.maxDelta = maxDelta;
            this.maxVega = maxVega;
        }

        public BigDecimal getCurrentDelta() { return currentDelta; }
        public BigDecimal getCurrentVega() { return currentVega; }
        public BigDecimal getMaxDelta() { return maxDelta; }
        public BigDecimal getMaxVega() { return maxVega; }
        
        public double getDeltaUtilization() { 
            return currentDelta.abs().divide(maxDelta, 4, RoundingMode.HALF_UP).doubleValue(); 
        }
        
        public double getVegaUtilization() { 
            return currentVega.abs().divide(maxVega, 4, RoundingMode.HALF_UP).doubleValue(); 
        }
    }
}