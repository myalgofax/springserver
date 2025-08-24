package com.myalgofax.trading;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Trade Outcome entity for ML model feedback loop
 * Stores predictions and actual outcomes for model performance tracking
 */
@Table("trade_outcomes")
public class TradeOutcome {
    
    @Id
    private Long id;
    
    @Column("strategy_id")
    private String strategyId;
    
    @Column("user_id")
    private String userId;
    
    @Column("predicted_pop")
    private BigDecimal predictedPop;
    
    @Column("confidence")
    private BigDecimal confidence;
    
    @Column("model_version")
    private String modelVersion;
    
    @Column("features")
    private Map<String, Object> features;
    
    @Column("position_size")
    private BigDecimal positionSize;
    
    @Column("prediction_time")
    private LocalDateTime predictionTime;
    
    @Column("close_time")
    private LocalDateTime closeTime;
    
    @Column("realized_pnl")
    private BigDecimal realizedPnl;
    
    @Column("actual_profit")
    private Boolean actualProfit;
    
    @Column("status")
    private String status; // OPEN, CLOSED
    
    // Constructors
    public TradeOutcome() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStrategyId() { return strategyId; }
    public void setStrategyId(String strategyId) { this.strategyId = strategyId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public BigDecimal getPredictedPop() { return predictedPop; }
    public void setPredictedPop(BigDecimal predictedPop) { this.predictedPop = predictedPop; }
    
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }
    
    public BigDecimal getPositionSize() { return positionSize; }
    public void setPositionSize(BigDecimal positionSize) { this.positionSize = positionSize; }
    
    public LocalDateTime getPredictionTime() { return predictionTime; }
    public void setPredictionTime(LocalDateTime predictionTime) { this.predictionTime = predictionTime; }
    
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    
    public BigDecimal getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(BigDecimal realizedPnl) { this.realizedPnl = realizedPnl; }
    
    public Boolean getActualProfit() { return actualProfit; }
    public void setActualProfit(Boolean actualProfit) { this.actualProfit = actualProfit; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}