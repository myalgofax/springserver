package com.myalgofax.dto;

import java.util.Map;

public class StrategyConfigDto {
    private String strategyId;
    private String strategyType;
    private String symbol;
    private Map<String, Object> parameters;
    private boolean isActive;
    private String userToken;

    public enum StrategyType {
        MOVING_AVERAGE_CROSSOVER, RSI_STRATEGY, MACD_STRATEGY, 
        BOLLINGER_BANDS, BREAKOUT_STRATEGY, ARBITRAGE, 
        MARKET_MAKING, CUSTOM
    }

    public String getStrategyId() { return strategyId; }
    public void setStrategyId(String strategyId) { this.strategyId = strategyId; }

    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getUserToken() { return userToken; }
    public void setUserToken(String userToken) { this.userToken = userToken; }
}