package com.myalgofax.ui.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @GetMapping("/recent")
    public Flux<Map<String, Object>> getRecentOrders(@RequestParam(defaultValue = "50") int limit) {
        return Flux.range(1, limit)
            .map(i -> Map.of(
                "orderId", "ORD_" + System.currentTimeMillis() + "_" + i,
                "strategyId", "IRON_CONDOR_" + (i % 3),
                "symbol", "NIFTY",
                "side", i % 2 == 0 ? "BUY" : "SELL",
                "quantity", 50 + (i % 100),
                "price", 18500.0 + Math.random() * 100,
                "status", List.of("FILLED", "PENDING", "CANCELLED").get(i % 3),
                "timestamp", LocalDateTime.now().minusMinutes(i * 5)
            ));
    }
    
    @GetMapping("/status/{orderId}")
    public Mono<Map<String, Object>> getOrderStatus(@PathVariable String orderId) {
        return Mono.just(Map.of(
            "orderId", orderId,
            "status", "FILLED",
            "fillPrice", 18525.0,
            "fillQuantity", 50,
            "remainingQuantity", 0,
            "fillTime", LocalDateTime.now()
        ));
    }
    
    @GetMapping("/execution-quality")
    public Mono<Map<String, Object>> getExecutionQuality() {
        return Mono.just(Map.of(
            "avgSlippage", 0.02,
            "fillRate", 0.95,
            "avgLatency", 125.0,
            "implementationShortfall", 0.015,
            "period", "LAST_24H"
        ));
    }
    
    @PostMapping("/cancel/{orderId}")
    public Mono<Map<String, Object>> cancelOrder(@PathVariable String orderId) {
        return Mono.just(Map.of(
            "orderId", orderId,
            "status", "CANCELLED",
            "message", "Order cancelled successfully"
        ));
    }
}