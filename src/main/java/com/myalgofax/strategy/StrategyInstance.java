package com.myalgofax.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.myalgofax.dto.StrategyConfigDto;

public class StrategyInstance {
    private String strategyId;
    private String strategyType;
    private String symbol;
    private String userId;
    private Map<String, Object> parameters;
    private boolean isActive;
    private boolean inPosition;
    private LocalDateTime deployedAt;
    private LocalDateTime lastUpdated;
    private BigDecimal totalPnL;
    private int executedTrades;

    public StrategyInstance(StrategyConfigDto config, String userId) {
        this.strategyId = config.getStrategyId();
        this.strategyType = config.getStrategyType();
        this.symbol = config.getSymbol();
        this.userId = userId;
        this.parameters = config.getParameters();
        this.isActive = config.isActive();
        this.inPosition = false;
        this.deployedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.totalPnL = BigDecimal.ZERO;
        this.executedTrades = 0;
    }

    public void updateConfig(StrategyConfigDto config) {
        this.parameters = config.getParameters();
        this.isActive = config.isActive();
        this.lastUpdated = LocalDateTime.now();
    }

    public String getStrategyId() { return strategyId; }
    public String getStrategyType() { return strategyType; }
    public String getSymbol() { return symbol; }
    public String getUserId() { return userId; }
    public Map<String, Object> getParameters() { return parameters; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public boolean isInPosition() { return inPosition; }
    public void setInPosition(boolean inPosition) { this.inPosition = inPosition; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public BigDecimal getTotalPnL() { return totalPnL; }
    public void setTotalPnL(BigDecimal totalPnL) { this.totalPnL = totalPnL; }
    public int getExecutedTrades() { return executedTrades; }
    public void incrementExecutedTrades() { this.executedTrades++; }
}