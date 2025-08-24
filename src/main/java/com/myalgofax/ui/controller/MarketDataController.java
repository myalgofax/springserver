package com.myalgofax.ui.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    @GetMapping("/symbols")
    public Flux<String> getWatchedSymbols() {
        return Flux.just("NIFTY", "BANKNIFTY", "RELIANCE", "TCS", "INFY");
    }
    
    @GetMapping("/price/{symbol}")
    public Mono<Map<String, Object>> getCurrentPrice(@PathVariable String symbol) {
        return Mono.just(Map.of(
            "symbol", symbol,
            "price", 18500.0 + Math.random() * 100,
            "change", Math.random() * 50 - 25,
            "changePercent", Math.random() * 2 - 1,
            "timestamp", LocalDateTime.now()
        ));
    }
    
    @GetMapping("/greeks/{symbol}")
    public Mono<Map<String, Double>> getGreeks(@PathVariable String symbol) {
        return Mono.just(Map.of(
            "delta", 0.5 + Math.random() * 0.3,
            "gamma", 0.1 + Math.random() * 0.05,
            "theta", -2.0 - Math.random() * 1.0,
            "vega", 15.0 + Math.random() * 5.0,
            "rho", 0.05 + Math.random() * 0.02
        ));
    }
    
    @GetMapping("/volatility/{symbol}")
    public Mono<Map<String, Object>> getVolatilityData(@PathVariable String symbol) {
        return Mono.just(Map.of(
            "symbol", symbol,
            "impliedVolatility", 0.15 + Math.random() * 0.1,
            "historicalVolatility", 0.12 + Math.random() * 0.08,
            "volatilityRank", Math.random() * 100,
            "timestamp", LocalDateTime.now()
        ));
    }
}