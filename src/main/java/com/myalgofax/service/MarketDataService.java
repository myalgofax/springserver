package com.myalgofax.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.myalgofax.dto.MarketDataDto;
import com.myalgofax.strategy.StrategyExecutionEngine;
import com.myalgofax.strategy.TechnicalIndicatorService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Service
public class MarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    
    private final StrategyExecutionEngine executionEngine;
    private final TechnicalIndicatorService indicatorService;
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();
    private final Sinks.Many<MarketDataDto> marketDataSink = Sinks.many().multicast().onBackpressureBuffer();

    public MarketDataService(StrategyExecutionEngine executionEngine, TechnicalIndicatorService indicatorService) {
        this.executionEngine = executionEngine;
        this.indicatorService = indicatorService;
        
        // Start market data processing
        startMarketDataProcessing();
    }

    public void subscribeToSymbol(String symbol) {
        subscribedSymbols.add(symbol);
        logger.info("Subscribed to market data for symbol: {}", symbol);
    }

    public void unsubscribeFromSymbol(String symbol) {
        subscribedSymbols.remove(symbol);
        logger.info("Unsubscribed from market data for symbol: {}", symbol);
    }

    public void publishMarketData(MarketDataDto marketData) {
        if (subscribedSymbols.contains(marketData.getSymbol())) {
            // Update technical indicators
            indicatorService.updatePrice(marketData.getSymbol(), marketData.getPrice(), marketData.getVolume());
            
            // Process through strategy engine
            executionEngine.processMarketData(marketData);
            
            // Emit to subscribers
            marketDataSink.tryEmitNext(marketData);
        }
    }

    public Flux<MarketDataDto> getMarketDataStream() {
        return marketDataSink.asFlux();
    }

    private void startMarketDataProcessing() {
        // Simulate market data feed - replace with actual market data connection
        Flux.interval(java.time.Duration.ofSeconds(1))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(tick -> {
                subscribedSymbols.forEach(symbol -> {
                    // Simulate price movement
                    BigDecimal basePrice = BigDecimal.valueOf(100 + Math.random() * 50);
                    BigDecimal volume = BigDecimal.valueOf(1000 + Math.random() * 5000);
                    
                    MarketDataDto marketData = new MarketDataDto(symbol, basePrice, volume);
                    publishMarketData(marketData);
                });
            });
    }

    public Set<String> getSubscribedSymbols() {
        return subscribedSymbols;
    }
}