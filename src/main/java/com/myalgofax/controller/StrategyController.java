package com.myalgofax.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.dto.StrategyConfigDto;
import com.myalgofax.service.MarketDataService;
import com.myalgofax.service.RiskManagementService;
import com.myalgofax.strategy.StrategyExecutionEngine;
import com.myalgofax.strategy.StrategyInstance;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    private final StrategyExecutionEngine executionEngine;
    private final MarketDataService marketDataService;
    private final RiskManagementService riskService;

    public StrategyController(StrategyExecutionEngine executionEngine, MarketDataService marketDataService, RiskManagementService riskService) {
        this.executionEngine = executionEngine;
        this.marketDataService = marketDataService;
        this.riskService = riskService;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> deployStrategy(@RequestBody StrategyConfigDto config) {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getCredentials())
            .flatMap(userId -> {
                // Subscribe to symbol for market data
                marketDataService.subscribeToSymbol(config.getSymbol());
                
                return executionEngine.deployStrategy(config, userId)
                    .map(strategyId -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("strategyId", strategyId);
                        response.put("status", "deployed");
                        response.put("symbol", config.getSymbol());
                        return ResponseEntity.ok(response);
                    });
            })
            .onErrorResume(error -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", error.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(errorResponse));
            });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> updateStrategy(@PathVariable String id, @RequestBody StrategyConfigDto config) {
        config.setStrategyId(id);
        
        return executionEngine.updateStrategy(id, config)
            .then(Mono.fromCallable(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("strategyId", id);
                response.put("status", "updated");
                return ResponseEntity.ok(response);
            }))
            .onErrorResume(error -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", error.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(errorResponse));
            });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> deactivateStrategy(@PathVariable String id) {
        return executionEngine.deactivateStrategy(id)
            .then(Mono.fromCallable(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("strategyId", id);
                response.put("status", "deactivated");
                return ResponseEntity.ok(response);
            }))
            .onErrorResume(error -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", error.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(errorResponse));
            });
    }

    @GetMapping("/performance")
    public Mono<ResponseEntity<Map<String, Object>>> getPerformance() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getCredentials())
            .map(userId -> {
                Map<String, Object> performance = new HashMap<>();
                
                // Get user strategies
                Map<String, StrategyInstance> userStrategies = executionEngine.getActiveStrategies()
                    .entrySet().stream()
                    .filter(entry -> entry.getValue().getUserId().equals(userId))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                
                performance.put("activeStrategies", userStrategies.size());
                performance.put("dailyPnL", riskService.getUserDailyPnL(userId));
                performance.put("dailyTrades", riskService.getUserDailyTrades(userId));
                performance.put("circuitBreakerTriggered", riskService.isCircuitBreakerTriggered(userId));
                
                // Strategy details
                Map<String, Object> strategyDetails = userStrategies.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            StrategyInstance strategy = entry.getValue();
                            Map<String, Object> details = new HashMap<>();
                            details.put("symbol", strategy.getSymbol());
                            details.put("type", strategy.getStrategyType());
                            details.put("isActive", strategy.isActive());
                            details.put("inPosition", strategy.isInPosition());
                            details.put("totalPnL", strategy.getTotalPnL());
                            details.put("executedTrades", strategy.getExecutedTrades());
                            details.put("deployedAt", strategy.getDeployedAt());
                            return details;
                        }
                    ));
                
                performance.put("strategies", strategyDetails);
                performance.put("subscribedSymbols", marketDataService.getSubscribedSymbols());
                
                return ResponseEntity.ok(performance);
            });
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalActiveStrategies", executionEngine.getActiveStrategies().size());
        status.put("subscribedSymbols", marketDataService.getSubscribedSymbols().size());
        status.put("systemStatus", "running");
        
        return ResponseEntity.ok(status);
    }
}