package com.myalgofax.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SignalDto {
    private String strategyId;
    private String symbol;
    private SignalType type;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private LocalDateTime timestamp;
    private String reason;

    public enum SignalType {
        BUY, SELL, HOLD, CLOSE_POSITION
    }

    public SignalDto() {
        this.timestamp = LocalDateTime.now();
    }

    public SignalDto(String strategyId, String symbol, SignalType type, BigDecimal price, BigDecimal quantity) {
        this.strategyId = strategyId;
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    public String getStrategyId() { return strategyId; }
    public void setStrategyId(String strategyId) { this.strategyId = strategyId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public SignalType getType() { return type; }
    public void setType(SignalType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getStopLoss() { return stopLoss; }
    public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }

    public BigDecimal getTakeProfit() { return takeProfit; }
    public void setTakeProfit(BigDecimal takeProfit) { this.takeProfit = takeProfit; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}