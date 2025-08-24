package com.myalgofax.ui.controller;

import com.myalgofax.ui.dto.*;
import com.myalgofax.ui.analytics.PerformanceAnalyticsService;
import com.myalgofax.strategy.StrategyExecutionEngine;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategies")
@CrossOrigin(origins = "*")
public class StrategyManagementController {
    
    private final StrategyExecutionEngine executionEngine;
    private final PerformanceAnalyticsService analyticsService;
    
    public StrategyManagementController(StrategyExecutionEngine executionEngine, 
                                     PerformanceAnalyticsService analyticsService) {
        this.executionEngine = executionEngine;
        this.analyticsService = analyticsService;
    }
    
    @GetMapping
    public Flux<String> getAvailableStrategies() {
        return Flux.just("IRON_CONDOR", "BULL_CALL_SPREAD", "BEAR_PUT_SPREAD", "STRADDLE");
    }
    
    @PostMapping("/instances")
    public Mono<StrategyInstanceDTO> deployStrategy(@RequestBody StrategyConfig config) {
        return executionEngine.deployStrategy(config)
            .map(this::toStrategyInstanceDTO);
    }
    
    @GetMapping("/instances")
    public Flux<StrategyInstanceDTO> getRunningStrategies() {
        return Flux.fromIterable(executionEngine.getActiveStrategies().values())
            .map(this::toStrategyInstanceDTO);
    }
    
    @GetMapping("/instances/{id}")
    public Mono<StrategyInstanceDTO> getStrategy(@PathVariable String id) {
        return Mono.justOrEmpty(executionEngine.getActiveStrategies().get(id))
            .map(this::toStrategyInstanceDTO);
    }
    
    @PutMapping("/instances/{id}/config")
    public Mono<StrategyInstanceDTO> updateStrategyConfig(@PathVariable String id, 
                                                        @RequestBody StrategyConfig config) {
        return executionEngine.updateStrategyConfig(id, config)
            .map(this::toStrategyInstanceDTO);
    }
    
    @PostMapping("/instances/{id}/pause")
    public Mono<Void> pauseStrategy(@PathVariable String id) {
        return executionEngine.pauseStrategy(id);
    }
    
    @PostMapping("/instances/{id}/resume")
    public Mono<Void> resumeStrategy(@PathVariable String id) {
        return executionEngine.resumeStrategy(id);
    }
    
    @DeleteMapping("/instances/{id}")
    public Mono<Void> shutdownStrategy(@PathVariable String id) {
        return executionEngine.shutdownStrategy(id);
    }
    
    private StrategyInstanceDTO toStrategyInstanceDTO(Object strategy) {
        // Simplified conversion - in practice would map from actual strategy object
        return new StrategyInstanceDTO(
            "strategy_id",
            "Strategy Name",
            "NIFTY",
            StrategyInstanceDTO.StrategyStatus.RUNNING,
            1500.0,
            800.0,
            700.0,
            java.time.LocalDateTime.now(),
            Map.of("delta", 0.5, "gamma", 0.1, "theta", -2.0, "vega", 15.0),
            new StrategyConfig("IRON_CONDOR", "NIFTY", Map.of(), 10000.0, 0.02),
            List.of()
        );
    }
}