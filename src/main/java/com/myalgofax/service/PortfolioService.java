package com.myalgofax.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myalgofax.dto.BrokerDto;
import reactor.core.publisher.Mono;

@Service
public class PortfolioService {
    
    private final Map<String, Position> positions = new ConcurrentHashMap<>();    
    @Autowired
    private BrokerService brokerService;

    public Mono<Position> getPosition(String symbol) {
        return Mono.fromCallable(() -> positions.get(symbol));
    }

    public Mono<Void> updatePosition(String symbol, BigDecimal quantity, BigDecimal entryPrice, 
                                   BigDecimal stopLoss, BigDecimal takeProfit) {
        return Mono.fromCallable(() -> {
            if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                positions.remove(symbol);
            } else {
                positions.put(symbol, new Position(quantity, entryPrice, stopLoss, takeProfit));
            }
            return null;
        });
    }
    
    public Mono<Map<String, Object>> getPortfolioHoldings(BrokerDto brokerDto) {
        return brokerService.getPortfolioHoldings(brokerDto);
    }
    
    public Mono<Double> calculatePortfolioValue() {
        return Mono.fromCallable(() -> {
            return positions.values().stream()
                .mapToDouble(pos -> pos.getQuantity().doubleValue() * pos.getEntryPrice().doubleValue())
                .sum();
        });
    }
    
    public Mono<Map<String, Double>> getPortfolioGreeks() {
        return Mono.fromCallable(() -> {
            Map<String, Double> greeks = new HashMap<>();
            greeks.put("delta", 0.5);
            greeks.put("gamma", 0.1);
            greeks.put("theta", -2.0);
            greeks.put("vega", 15.0);
            return greeks;
        });
    }

    public static class Position {
        private final BigDecimal quantity;
        private final BigDecimal entryPrice;
        private final BigDecimal stopLoss;
        private final BigDecimal takeProfit;

        public Position(BigDecimal quantity, BigDecimal entryPrice, BigDecimal stopLoss, BigDecimal takeProfit) {
            this.quantity = quantity;
            this.entryPrice = entryPrice;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
        }

        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getEntryPrice() { return entryPrice; }
        public BigDecimal getStopLoss() { return stopLoss; }
        public BigDecimal getTakeProfit() { return takeProfit; }
    }
}