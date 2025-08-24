package com.myalgofax.meta;

import com.myalgofax.meta.portfolio.MetaStrategyManager;
import com.myalgofax.meta.optimization.StrategyGenerator;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/meta-strategy")
@CrossOrigin(origins = "*")
public class MetaStrategyController {
    
    private final MetaStrategyManager metaStrategyManager;
    private final StrategyGenerator strategyGenerator;
    
    public MetaStrategyController(MetaStrategyManager metaStrategyManager, StrategyGenerator strategyGenerator) {
        this.metaStrategyManager = metaStrategyManager;
        this.strategyGenerator = strategyGenerator;
    }
    
    @PostMapping("/rebalance")
    public Mono<Map<String, Double>> rebalancePortfolio(@RequestParam double totalCapital) {
        return metaStrategyManager.rebalancePortfolio(totalCapital);
    }
    
    @GetMapping("/allocations")
    public Mono<Map<String, Double>> getCurrentAllocations() {
        return Mono.just(metaStrategyManager.getCurrentAllocations());
    }
    
    @GetMapping("/performance")
    public Mono<Map<String, MetaStrategyManager.StrategyPerformance>> getStrategyPerformances() {
        return Mono.just(metaStrategyManager.getStrategyPerformances());
    }
    
    @GetMapping("/portfolio-sharpe")
    public Mono<Double> getPortfolioSharpeRatio() {
        return metaStrategyManager.calculatePortfolioSharpeRatio();
    }
    
    @GetMapping("/underperforming")
    public Mono<List<String>> getUnderperformingStrategies(
            @RequestParam(defaultValue = "0.5") double minSharpeRatio,
            @RequestParam(defaultValue = "0.2") double maxDrawdownThreshold) {
        return metaStrategyManager.identifyUnderperformingStrategies(minSharpeRatio, maxDrawdownThreshold);
    }
    
    @GetMapping("/top-performing/{count}")
    public Mono<List<String>> getTopPerformingStrategies(@PathVariable int count) {
        return metaStrategyManager.identifyTopPerformingStrategies(count);
    }
    
    @PostMapping("/update-performance")
    public Mono<Void> updateStrategyPerformance(
            @RequestParam String strategyId,
            @RequestParam double dailyReturn,
            @RequestParam int trades,
            @RequestParam double winRate) {
        return metaStrategyManager.updateStrategyPerformance(strategyId, dailyReturn, trades, winRate);
    }
    
    @GetMapping("/should-rebalance")
    public Mono<Boolean> shouldRebalance() {
        return metaStrategyManager.shouldRebalance();
    }
    
    @PostMapping("/generate-strategies")
    public Flux<StrategyGenerator.StrategyTemplate> generateStrategies(
            @RequestParam(defaultValue = "100") int numStrategies,
            @RequestParam(defaultValue = "252") int backtestDays) {
        return strategyGenerator.generateAndTestStrategies(numStrategies, backtestDays);
    }
    
    @PostMapping("/backtest-strategy")
    public Mono<StrategyGenerator.BacktestResult> backtestStrategy(
            @RequestBody StrategyGenerator.StrategyTemplate strategy,
            @RequestParam(defaultValue = "252") int backtestDays) {
        return strategyGenerator.backtestStrategy(strategy, backtestDays);
    }
}