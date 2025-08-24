package com.myalgofax.execution.routing;

import java.time.LocalDate;

public class OptionContract {
    private String symbol;
    private LocalDate expiry;
    private double strike;
    private String optionType; // CALL or PUT
    private String exchange;
    
    public OptionContract(String symbol, LocalDate expiry, double strike, String optionType) {
        this.symbol = symbol;
        this.expiry = expiry;
        this.strike = strike;
        this.optionType = optionType;
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }
    
    public double getStrike() { return strike; }
    public void setStrike(double strike) { this.strike = strike; }
    
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    
    @Override
    public String toString() {
        return String.format("%s_%s_%s_%.2f", symbol, expiry, optionType, strike);
    }
}