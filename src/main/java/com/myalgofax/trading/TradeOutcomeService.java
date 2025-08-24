package com.myalgofax.trading;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;

import com.myalgofax.trading.MLPredictionService.MLPredictionResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Trade Outcome Service for ML model performance feedback loop
 * Records predictions and actual outcomes for model retraining
 */
@Service
public class TradeOutcomeService {
    
    private static final Logger logger = LoggerFactory.getLogger(TradeOutcomeService.class);
    
    private final R2dbcEntityTemplate r2dbcTemplate;

    public TradeOutcomeService(R2dbcEntityTemplate r2dbcTemplate) {
        this.r2dbcTemplate = r2dbcTemplate;
    }

    /**
     * Record ML prediction when strategy enters position
     */
    public Mono<Void> recordPrediction(String strategyId, String userId, MLPredictionResult prediction, 
                                     Map<String, Object> features, BigDecimal positionSize) {
        TradeOutcome outcome = new TradeOutcome();
        outcome.setStrategyId(strategyId);
        outcome.setUserId(userId);
        outcome.setPredictedPop(BigDecimal.valueOf(prediction.getProbabilityOfProfit()));
        outcome.setConfidence(BigDecimal.valueOf(prediction.getConfidence()));
        outcome.setModelVersion(prediction.getModelVersion());
        outcome.setFeatures(features);
        outcome.setPositionSize(positionSize);
        outcome.setPredictionTime(LocalDateTime.now());
        outcome.setStatus("OPEN");
        
        return r2dbcTemplate.insert(outcome)
            .doOnSuccess(saved -> logger.info("Recorded ML prediction for strategy {}: POP={}", 
                strategyId, prediction.getProbabilityOfProfit()))
            .then();
    }

    /**
     * Update outcome when position is closed
     */
    public Mono<Void> recordOutcome(String strategyId, BigDecimal realizedPnL, LocalDateTime closeTime) {
        return r2dbcTemplate.select(TradeOutcome.class)
            .matching(Query.query(org.springframework.data.relational.core.query.Criteria
                .where("strategy_id").is(strategyId)
                .and("status").is("OPEN")))
            .first()
            .flatMap(outcome -> {
                outcome.setRealizedPnl(realizedPnL);
                outcome.setCloseTime(closeTime);
                outcome.setStatus("CLOSED");
                outcome.setActualProfit(realizedPnL.compareTo(BigDecimal.ZERO) > 0);
                
                return r2dbcTemplate.update(outcome);
            })
            .doOnSuccess(updated -> logger.info("Updated trade outcome for strategy {}: PnL={}", 
                strategyId, realizedPnL))
            .then();
    }

    /**
     * Calculate model accuracy metrics (Brier Score)
     * Brier Score = (1/N) * Σ(predicted_prob - actual_outcome)²
     * Lower is better, perfect score = 0
     */
    public Mono<ModelPerformanceReport> generatePerformanceReport(String modelVersion, LocalDateTime fromDate) {
        return r2dbcTemplate.select(TradeOutcome.class)
            .matching(Query.query(org.springframework.data.relational.core.query.Criteria
                .where("model_version").is(modelVersion)
                .and("status").is("CLOSED")
                .and("prediction_time").greaterThan(fromDate)))
            .all()
            .collectList()
            .map(outcomes -> {
                if (outcomes.isEmpty()) {
                    return new ModelPerformanceReport(modelVersion, 0, 0.0, 0.0, 0.0, 0.0);
                }
                
                int totalTrades = outcomes.size();
                double totalBrierScore = 0.0;
                int correctPredictions = 0;
                double totalPnL = 0.0;
                
                for (TradeOutcome outcome : outcomes) {
                    double predictedProb = outcome.getPredictedPop().doubleValue();
                    boolean actualProfit = outcome.getActualProfit();
                    double actualOutcome = actualProfit ? 1.0 : 0.0;
                    
                    // Brier Score calculation
                    double brierScore = Math.pow(predictedProb - actualOutcome, 2);
                    totalBrierScore += brierScore;
                    
                    // Accuracy (predicted > 0.5 and actual profit, or predicted <= 0.5 and actual loss)
                    if ((predictedProb > 0.5 && actualProfit) || (predictedProb <= 0.5 && !actualProfit)) {
                        correctPredictions++;
                    }
                    
                    totalPnL += outcome.getRealizedPnl().doubleValue();
                }
                
                double avgBrierScore = totalBrierScore / totalTrades;
                double accuracy = (double) correctPredictions / totalTrades;
                double avgPnL = totalPnL / totalTrades;
                double winRate = outcomes.stream()
                    .mapToDouble(o -> o.getActualProfit() ? 1.0 : 0.0)
                    .average()
                    .orElse(0.0);
                
                return new ModelPerformanceReport(modelVersion, totalTrades, avgBrierScore, 
                    accuracy, avgPnL, winRate);
            })
            .doOnNext(report -> logger.info("Generated performance report for model {}: " +
                "Trades={}, Brier={}, Accuracy={}, AvgPnL={}, WinRate={}", 
                modelVersion, report.getTotalTrades(), report.getBrierScore(), 
                report.getAccuracy(), report.getAveragePnL(), report.getWinRate()));
    }

    /**
     * Get training data for model retraining
     */
    public Flux<TradeOutcome> getTrainingData(LocalDateTime fromDate, int limit) {
        return r2dbcTemplate.select(TradeOutcome.class)
            .matching(Query.query(org.springframework.data.relational.core.query.Criteria
                .where("status").is("CLOSED")
                .and("prediction_time").greaterThan(fromDate))
                .limit(limit))
            .all();
    }

    /**
     * Model Performance Report
     */
    public static class ModelPerformanceReport {
        private final String modelVersion;
        private final int totalTrades;
        private final double brierScore;
        private final double accuracy;
        private final double averagePnL;
        private final double winRate;

        public ModelPerformanceReport(String modelVersion, int totalTrades, double brierScore, 
                                    double accuracy, double averagePnL, double winRate) {
            this.modelVersion = modelVersion;
            this.totalTrades = totalTrades;
            this.brierScore = brierScore;
            this.accuracy = accuracy;
            this.averagePnL = averagePnL;
            this.winRate = winRate;
        }

        public String getModelVersion() { return modelVersion; }
        public int getTotalTrades() { return totalTrades; }
        public double getBrierScore() { return brierScore; }
        public double getAccuracy() { return accuracy; }
        public double getAveragePnL() { return averagePnL; }
        public double getWinRate() { return winRate; }
        
        public String getPerformanceGrade() {
            if (brierScore < 0.1 && accuracy > 0.7) return "EXCELLENT";
            if (brierScore < 0.2 && accuracy > 0.6) return "GOOD";
            if (brierScore < 0.3 && accuracy > 0.5) return "FAIR";
            return "POOR";
        }
    }
}