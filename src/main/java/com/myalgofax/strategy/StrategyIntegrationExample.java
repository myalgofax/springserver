package com.myalgofax.strategy;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.myalgofax.dto.MarketDataDto;
import com.myalgofax.dto.SignalDto;

import reactor.core.publisher.Mono;

/**
 * Example integration showing how to use EMACrossoverWithRSIStrategy
 * within the existing strategy execution framework
 */
@Component
public class StrategyIntegrationExample {
    
    private final EMACrossoverWithRSIStrategy emaCrossoverStrategy;
    
    public StrategyIntegrationExample(EMACrossoverWithRSIStrategy emaCrossoverStrategy) {
        this.emaCrossoverStrategy = emaCrossoverStrategy;
    }
    
    /**
     * Example method showing how to integrate the EMA Crossover strategy
     * with the existing strategy execution engine
     */
    public Mono<SignalDto> executeEMACrossoverStrategy(MarketDataDto marketData, Map<String, Object> parameters) {
        // Validate required parameters
        if (!parameters.containsKey("symbol")) {
            return Mono.error(new IllegalArgumentException("Symbol parameter is required"));
        }
        
        // Execute the strategy
        return emaCrossoverStrategy.evaluate(marketData, parameters)
            .doOnNext(signal -> {
                // Log the generated signal
                System.out.println("EMA Crossover Strategy Signal: " + signal.getType() + 
                                 " for " + signal.getSymbol() + " at " + signal.getPrice());
            })
            .doOnError(error -> {
                // Log any errors
                System.err.println("EMA Crossover Strategy Error: " + error.getMessage());
            });
    }
    
    /**
     * Example configuration for EMA Crossover strategy
     */
    public static Map<String, Object> getDefaultEMACrossoverConfig(String symbol) {
        return Map.of(
            "symbol", symbol,
            "fastEmaPeriod", 9,
            "slowEmaPeriod", 21,
            "rsiPeriod", 14,
            "atrPeriod", 14,
            "riskRewardRatio", 2.0,
            "volumePeriod", 20
        );
    }
}