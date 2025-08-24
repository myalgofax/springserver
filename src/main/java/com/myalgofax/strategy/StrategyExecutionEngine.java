package com.myalgofax.strategy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.myalgofax.dto.MarketDataDto;
import com.myalgofax.dto.SignalDto;
import com.myalgofax.dto.StrategyConfigDto;
import com.myalgofax.service.BrokerService;
import com.myalgofax.ui.dto.StrategyConfig;
import com.myalgofax.ui.websocket.DashboardWebSocketHandler;
import com.myalgofax.ui.analytics.PerformanceAnalyticsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Component
public class StrategyExecutionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(StrategyExecutionEngine.class);
    
    private final Map<String, StrategyInstance> activeStrategies = new ConcurrentHashMap<>();
    private final Sinks.Many<SignalDto> signalSink = Sinks.many().multicast().onBackpressureBuffer();
    private final BrokerService brokerService;
    private final TechnicalIndicatorService indicatorService;
    private final DashboardWebSocketHandler dashboardHandler;
    private final PerformanceAnalyticsService analyticsService;
    
    public StrategyExecutionEngine(BrokerService brokerService, TechnicalIndicatorService indicatorService,
                                 DashboardWebSocketHandler dashboardHandler, PerformanceAnalyticsService analyticsService) {
        this.brokerService = brokerService;
        this.indicatorService = indicatorService;
        this.dashboardHandler = dashboardHandler;
        this.analyticsService = analyticsService;
    }

    public Mono<String> deployStrategy(StrategyConfigDto config, String userId) {
        return Mono.fromCallable(() -> {
            StrategyInstance instance = new StrategyInstance(config, userId);
            activeStrategies.put(config.getStrategyId(), instance);
            logger.info("Strategy deployed: {} for user: {}", config.getStrategyId(), userId);
            return config.getStrategyId();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> updateStrategy(String strategyId, StrategyConfigDto config) {
        return Mono.fromCallable(() -> {
            StrategyInstance instance = activeStrategies.get(strategyId);
            if (instance != null) {
                instance.updateConfig(config);
                logger.info("Strategy updated: {}", strategyId);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> deactivateStrategy(String strategyId) {
        return Mono.fromCallable(() -> {
            activeStrategies.remove(strategyId);
            logger.info("Strategy deactivated: {}", strategyId);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public void processMarketData(MarketDataDto marketData) {
        activeStrategies.values().parallelStream()
            .filter(strategy -> strategy.getSymbol().equals(marketData.getSymbol()))
            .filter(StrategyInstance::isActive)
            .forEach(strategy -> evaluateStrategy(strategy, marketData));
    }

    private void evaluateStrategy(StrategyInstance strategy, MarketDataDto marketData) {
        try {
            switch (strategy.getStrategyType()) {
                case "MOVING_AVERAGE_CROSSOVER":
                    evaluateMovingAverageCrossover(strategy, marketData);
                    break;
                case "RSI_STRATEGY":
                    evaluateRSIStrategy(strategy, marketData);
                    break;
                case "MACD_STRATEGY":
                    evaluateMACDStrategy(strategy, marketData);
                    break;
                case "BREAKOUT_STRATEGY":
                    evaluateBreakoutStrategy(strategy, marketData);
                    break;
                default:
                    logger.warn("Unknown strategy type: {}", strategy.getStrategyType());
            }
        } catch (Exception e) {
            logger.error("Error evaluating strategy {}: {}", strategy.getStrategyId(), e.getMessage());
        }
    }

    private void evaluateMovingAverageCrossover(StrategyInstance strategy, MarketDataDto marketData) {
        Map<String, Object> params = strategy.getParameters();
        int fastMA = (Integer) params.get("fastMA");
        int slowMA = (Integer) params.get("slowMA");
        
        BigDecimal fastMAValue = indicatorService.calculateSMA(marketData.getSymbol(), fastMA);
        BigDecimal slowMAValue = indicatorService.calculateSMA(marketData.getSymbol(), slowMA);
        
        if (fastMAValue != null && slowMAValue != null) {
            if (fastMAValue.compareTo(slowMAValue) > 0 && !strategy.isInPosition()) {
                generateBuySignal(strategy, marketData, "Fast MA crossed above Slow MA");
            } else if (fastMAValue.compareTo(slowMAValue) < 0 && strategy.isInPosition()) {
                generateSellSignal(strategy, marketData, "Fast MA crossed below Slow MA");
            }
        }
    }

    private void evaluateRSIStrategy(StrategyInstance strategy, MarketDataDto marketData) {
        BigDecimal rsi = indicatorService.calculateRSI(marketData.getSymbol(), 14);
        
        if (rsi != null) {
            if (rsi.compareTo(BigDecimal.valueOf(30)) < 0 && !strategy.isInPosition()) {
                generateBuySignal(strategy, marketData, "RSI oversold");
            } else if (rsi.compareTo(BigDecimal.valueOf(70)) > 0 && strategy.isInPosition()) {
                generateSellSignal(strategy, marketData, "RSI overbought");
            }
        }
    }

    private void evaluateMACDStrategy(StrategyInstance strategy, MarketDataDto marketData) {
        Map<String, BigDecimal> macd = indicatorService.calculateMACD(marketData.getSymbol());
        
        if (macd != null && macd.containsKey("macd") && macd.containsKey("signal")) {
            BigDecimal macdLine = macd.get("macd");
            BigDecimal signalLine = macd.get("signal");
            
            if (macdLine.compareTo(signalLine) > 0 && !strategy.isInPosition()) {
                generateBuySignal(strategy, marketData, "MACD bullish crossover");
            } else if (macdLine.compareTo(signalLine) < 0 && strategy.isInPosition()) {
                generateSellSignal(strategy, marketData, "MACD bearish crossover");
            }
        }
    }

    private void evaluateBreakoutStrategy(StrategyInstance strategy, MarketDataDto marketData) {
        Map<String, Object> params = strategy.getParameters();
        int period = (Integer) params.getOrDefault("period", 20);
        
        BigDecimal resistance = indicatorService.getResistanceLevel(marketData.getSymbol(), period);
        BigDecimal support = indicatorService.getSupportLevel(marketData.getSymbol(), period);
        
        if (resistance != null && marketData.getPrice().compareTo(resistance) > 0 && !strategy.isInPosition()) {
            generateBuySignal(strategy, marketData, "Price broke above resistance");
        } else if (support != null && marketData.getPrice().compareTo(support) < 0 && strategy.isInPosition()) {
            generateSellSignal(strategy, marketData, "Price broke below support");
        }
    }

    private void generateBuySignal(StrategyInstance strategy, MarketDataDto marketData, String reason) {
        Map<String, Object> params = strategy.getParameters();
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        BigDecimal stopLoss = params.containsKey("stopLoss") ? 
            new BigDecimal(params.get("stopLoss").toString()) : null;
        BigDecimal takeProfit = params.containsKey("takeProfit") ? 
            new BigDecimal(params.get("takeProfit").toString()) : null;

        SignalDto signal = new SignalDto(strategy.getStrategyId(), marketData.getSymbol(), 
            SignalDto.SignalType.BUY, marketData.getPrice(), quantity);
        signal.setStopLoss(stopLoss);
        signal.setTakeProfit(takeProfit);
        signal.setReason(reason);

        strategy.setInPosition(true);
        signalSink.tryEmitNext(signal);
        logger.info("Buy signal generated for strategy {}: {}", strategy.getStrategyId(), reason);
    }

    private void generateSellSignal(StrategyInstance strategy, MarketDataDto marketData, String reason) {
        Map<String, Object> params = strategy.getParameters();
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());

        SignalDto signal = new SignalDto(strategy.getStrategyId(), marketData.getSymbol(), 
            SignalDto.SignalType.SELL, marketData.getPrice(), quantity);
        signal.setReason(reason);

        strategy.setInPosition(false);
        signalSink.tryEmitNext(signal);
        logger.info("Sell signal generated for strategy {}: {}", strategy.getStrategyId(), reason);
    }

    public Flux<SignalDto> getSignalStream() {
        return signalSink.asFlux();
    }

    public Map<String, StrategyInstance> getActiveStrategies() {
        return activeStrategies;
    }
    
    // UI Integration Methods
    public Mono<StrategyInstance> deployStrategy(StrategyConfig config) {
        return Mono.fromCallable(() -> {
            StrategyConfigDto configDto = convertToConfigDto(config);
            StrategyInstance instance = new StrategyInstance(configDto, "UI_USER");
            activeStrategies.put(config.strategyType() + "_" + System.currentTimeMillis(), instance);
            
            dashboardHandler.broadcastStrategyUpdate(instance.getStrategyId(), 
                Map.of("status", "DEPLOYED", "config", config));
            
            return instance;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<StrategyInstance> updateStrategyConfig(String strategyId, StrategyConfig config) {
        return Mono.fromCallable(() -> {
            StrategyInstance instance = activeStrategies.get(strategyId);
            if (instance != null) {
                StrategyConfigDto configDto = convertToConfigDto(config);
                instance.updateConfig(configDto);
                
                dashboardHandler.broadcastStrategyUpdate(strategyId, 
                    Map.of("status", "UPDATED", "config", config));
            }
            return instance;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<Void> pauseStrategy(String strategyId) {
        return Mono.fromRunnable(() -> {
            StrategyInstance instance = activeStrategies.get(strategyId);
            if (instance != null) {
                instance.setActive(false);
                dashboardHandler.broadcastStrategyUpdate(strategyId, 
                    Map.of("status", "PAUSED"));
            }
        });
    }
    
    public Mono<Void> resumeStrategy(String strategyId) {
        return Mono.fromRunnable(() -> {
            StrategyInstance instance = activeStrategies.get(strategyId);
            if (instance != null) {
                instance.setActive(true);
                dashboardHandler.broadcastStrategyUpdate(strategyId, 
                    Map.of("status", "RESUMED"));
            }
        });
    }
    
    public Mono<Void> shutdownStrategy(String strategyId) {
        return Mono.fromRunnable(() -> {
            StrategyInstance instance = activeStrategies.remove(strategyId);
            if (instance != null) {
                instance.setActive(false);
                dashboardHandler.broadcastStrategyUpdate(strategyId, 
                    Map.of("status", "SHUTDOWN"));
            }
        });
    }
    
    private StrategyConfigDto convertToConfigDto(StrategyConfig config) {
        StrategyConfigDto dto = new StrategyConfigDto();
        dto.setStrategyId(config.strategyType() + "_" + System.currentTimeMillis());
        dto.setStrategyType(config.strategyType());
        dto.setSymbol(config.symbol());
        dto.setParameters(config.parameters());
        return dto;
    }
    
    public void updateStrategyPnL(String strategyId, double pnl) {
        analyticsService.updateStrategyPnL(strategyId, pnl).subscribe();
        dashboardHandler.broadcastPnLUpdate(Map.of(strategyId, pnl));
    }
}