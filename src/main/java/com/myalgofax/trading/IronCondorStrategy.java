package com.myalgofax.trading;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.myalgofax.dto.MarketDataDto;
import com.myalgofax.dto.SignalDto;
import com.myalgofax.trading.DynamicRiskManagerService.PositionSizeRequest;
import com.myalgofax.trading.MLPredictionService.MLPredictionResult;

import reactor.core.publisher.Mono;

/**
 * Iron Condor Options Strategy with ML Integration
 * Generates signals based on ML probability predictions and dynamic risk management
 */
@Component
public class IronCondorStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(IronCondorStrategy.class);
    
    private final MLPredictionService mlPredictionService;
    private final DynamicRiskManagerService riskManager;
    private final TradeOutcomeService tradeOutcomeService;

    public IronCondorStrategy(MLPredictionService mlPredictionService, 
                             DynamicRiskManagerService riskManager,
                             TradeOutcomeService tradeOutcomeService) {
        this.mlPredictionService = mlPredictionService;
        this.riskManager = riskManager;
        this.tradeOutcomeService = tradeOutcomeService;
    }

    /**
     * Evaluate Iron Condor strategy with ML prediction gate
     */
    public Mono<SignalDto> evaluate(MarketDataDto marketData, IronCondorParameters params, String userId) {
        return buildFeatureVector(marketData, params)
            .flatMap(mlPredictionService::getPrediction)
            .flatMap(prediction -> {
                // ML Gate: Only proceed if prediction meets criteria
                if (!prediction.shouldTrade()) {
                    logger.debug("ML gate rejected trade for {}: POP={}, confidence={}", 
                        marketData.getSymbol(), prediction.getProbabilityOfProfit(), prediction.getConfidence());
                    return Mono.empty();
                }
                
                return generateIronCondorSignal(marketData, params, prediction, userId);
            })
            .onErrorResume(error -> {
                logger.error("Iron Condor strategy evaluation failed: {}", error.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Build feature vector for ML model
     */
    private Mono<Map<String, Object>> buildFeatureVector(MarketDataDto marketData, IronCondorParameters params) {
        return Mono.fromCallable(() -> {
            Map<String, Object> features = new HashMap<>();
            
            // Market features
            features.put("underlying_price", marketData.getPrice().doubleValue());
            features.put("volume", marketData.getVolume().doubleValue());
            features.put("volatility", calculateImpliedVolatility(marketData));
            
            // Strategy features
            features.put("days_to_expiry", params.getDaysToExpiry());
            features.put("strike_width", params.getStrikeWidth());
            features.put("put_strike_distance", params.getPutStrikeDistance());
            features.put("call_strike_distance", params.getCallStrikeDistance());
            
            // Risk features
            features.put("max_profit", params.getMaxProfit().doubleValue());
            features.put("max_loss", params.getMaxLoss().doubleValue());
            features.put("profit_probability", params.getProfitProbability());
            
            // Market regime features
            features.put("market_trend", calculateMarketTrend(marketData));
            features.put("volatility_rank", calculateVolatilityRank(marketData));
            
            return features;
        });
    }

    /**
     * Generate Iron Condor signal with dynamic position sizing
     */
    private Mono<SignalDto> generateIronCondorSignal(MarketDataDto marketData, IronCondorParameters params, 
                                                    MLPredictionResult prediction, String userId) {
        
        // Calculate position size using Kelly Criterion
        PositionSizeRequest sizeRequest = new PositionSizeRequest(
            prediction,
            params.getMaxProfit(),
            params.getMaxLoss(),
            params.getAvailableCapital(),
            userId,
            params.getDeltaPerUnit(),
            params.getVegaPerUnit(),
            params.getLotSize()
        );
        
        return riskManager.calculatePositionSize(sizeRequest)
            .flatMap(sizeResult -> {
                if (sizeResult.getLotSize() == 0) {
                    logger.info("Position size too small for Iron Condor on {}", marketData.getSymbol());
                    return Mono.empty();
                }
                
                String strategyId = UUID.randomUUID().toString();
                
                // Create Iron Condor signal
                SignalDto signal = new SignalDto();
                signal.setStrategyId(strategyId);
                signal.setSymbol(marketData.getSymbol());
                signal.setType(SignalDto.SignalType.BUY); // Enter Iron Condor
                signal.setPrice(params.getNetCredit());
                signal.setQuantity(BigDecimal.valueOf(sizeResult.getLotSize()));
                signal.setReason(String.format("Iron Condor: ML POP=%.2f, Kelly lots=%d", 
                    prediction.getProbabilityOfProfit(), sizeResult.getLotSize()));
                
                // Record ML prediction for feedback loop
                return tradeOutcomeService.recordPrediction(strategyId, userId, prediction, 
                    buildFeatureVector(marketData, params).block(), sizeResult.getFinalPositionSize())
                    .thenReturn(signal);
            });
    }

    // Helper methods for feature calculation
    private double calculateImpliedVolatility(MarketDataDto marketData) {
        // Simplified IV calculation - in production, use proper options pricing model
        return 0.20 + (Math.random() * 0.30); // 20-50% IV range
    }

    private double calculateMarketTrend(MarketDataDto marketData) {
        // Simplified trend calculation - in production, use technical indicators
        return Math.random() > 0.5 ? 1.0 : -1.0; // Bullish or bearish
    }

    private double calculateVolatilityRank(MarketDataDto marketData) {
        // Simplified volatility rank - in production, use historical volatility percentile
        return Math.random(); // 0-1 range
    }

    /**
     * Iron Condor Strategy Parameters
     */
    public static class IronCondorParameters {
        private final int daysToExpiry;
        private final int strikeWidth;
        private final int putStrikeDistance;
        private final int callStrikeDistance;
        private final BigDecimal maxProfit;
        private final BigDecimal maxLoss;
        private final double profitProbability;
        private final BigDecimal availableCapital;
        private final BigDecimal deltaPerUnit;
        private final BigDecimal vegaPerUnit;
        private final int lotSize;
        private final BigDecimal netCredit;

        public IronCondorParameters(int daysToExpiry, int strikeWidth, int putStrikeDistance, 
                                  int callStrikeDistance, BigDecimal maxProfit, BigDecimal maxLoss,
                                  double profitProbability, BigDecimal availableCapital, 
                                  BigDecimal deltaPerUnit, BigDecimal vegaPerUnit, int lotSize, BigDecimal netCredit) {
            this.daysToExpiry = daysToExpiry;
            this.strikeWidth = strikeWidth;
            this.putStrikeDistance = putStrikeDistance;
            this.callStrikeDistance = callStrikeDistance;
            this.maxProfit = maxProfit;
            this.maxLoss = maxLoss;
            this.profitProbability = profitProbability;
            this.availableCapital = availableCapital;
            this.deltaPerUnit = deltaPerUnit;
            this.vegaPerUnit = vegaPerUnit;
            this.lotSize = lotSize;
            this.netCredit = netCredit;
        }

        // Getters
        public int getDaysToExpiry() { return daysToExpiry; }
        public int getStrikeWidth() { return strikeWidth; }
        public int getPutStrikeDistance() { return putStrikeDistance; }
        public int getCallStrikeDistance() { return callStrikeDistance; }
        public BigDecimal getMaxProfit() { return maxProfit; }
        public BigDecimal getMaxLoss() { return maxLoss; }
        public double getProfitProbability() { return profitProbability; }
        public BigDecimal getAvailableCapital() { return availableCapital; }
        public BigDecimal getDeltaPerUnit() { return deltaPerUnit; }
        public BigDecimal getVegaPerUnit() { return vegaPerUnit; }
        public int getLotSize() { return lotSize; }
        public BigDecimal getNetCredit() { return netCredit; }
    }
}