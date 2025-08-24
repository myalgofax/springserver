package com.myalgofax.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketDataDto {
    private String symbol;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private LocalDateTime timestamp;

    public MarketDataDto() {}

    public MarketDataDto(String symbol, BigDecimal price, BigDecimal volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = LocalDateTime.now();
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}