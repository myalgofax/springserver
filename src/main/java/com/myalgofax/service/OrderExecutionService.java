package com.myalgofax.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.myalgofax.dto.PlaceOrderDto;
import com.myalgofax.dto.SignalDto;
import com.myalgofax.strategy.StrategyExecutionEngine;
import com.myalgofax.strategy.StrategyInstance;
import com.myalgofax.execution.routing.SmartOrderRouterService;
import com.myalgofax.execution.routing.OptionContract;
import com.myalgofax.execution.routing.BrokerEndpoint;
import com.myalgofax.execution.SpreadExecutionService;
import com.myalgofax.execution.algorithms.*;
import com.myalgofax.execution.monitoring.TCAService;
import com.myalgofax.execution.monitoring.LatencyMonitoringService;
import com.myalgofax.trading.DynamicRiskManagerService;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
public class OrderExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderExecutionService.class);
    
    private final BrokerService brokerService;
    private final StrategyExecutionEngine executionEngine;
    private final RiskManagementService riskService;
    private final SmartOrderRouterService smartOrderRouter;
    private final SpreadExecutionService spreadExecutionService;
    private final DynamicRiskManagerService dynamicRiskManager;
    private final TCAService tcaService;
    private final LatencyMonitoringService latencyMonitor;
    private final VWAPAlgorithm vwapAlgorithm;
    private final TWAPAlgorithm twapAlgorithm;
    private final ImplementationShortfallAlgorithm isAlgorithm;

    public OrderExecutionService(BrokerService brokerService, StrategyExecutionEngine executionEngine, 
                               RiskManagementService riskService, SmartOrderRouterService smartOrderRouter,
                               SpreadExecutionService spreadExecutionService, DynamicRiskManagerService dynamicRiskManager,
                               TCAService tcaService, LatencyMonitoringService latencyMonitor,
                               VWAPAlgorithm vwapAlgorithm, TWAPAlgorithm twapAlgorithm, 
                               ImplementationShortfallAlgorithm isAlgorithm) {
        this.brokerService = brokerService;
        this.executionEngine = executionEngine;
        this.riskService = riskService;
        this.smartOrderRouter = smartOrderRouter;
        this.spreadExecutionService = spreadExecutionService;
        this.dynamicRiskManager = dynamicRiskManager;
        this.tcaService = tcaService;
        this.latencyMonitor = latencyMonitor;
        this.vwapAlgorithm = vwapAlgorithm;
        this.twapAlgorithm = twapAlgorithm;
        this.isAlgorithm = isAlgorithm;
        
        // Subscribe to signals from strategy engine
        executionEngine.getSignalStream()
            .flatMap(this::executeSignal)
            .subscribe(
                result -> logger.info("Order executed: {}", result),
                error -> logger.error("Order execution failed: {}", error.getMessage())
            );
    }

    private Mono<Map<String, Object>> executeSignal(SignalDto signal) {
        String orderId = generateOrderId();
        
        return latencyMonitor.startLatencyTracking(orderId)
            .then(latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.SIGNAL_GENERATION))
            .then(Mono.fromCallable(() -> {
                StrategyInstance strategy = executionEngine.getActiveStrategies().get(signal.getStrategyId());
                if (strategy == null) {
                    throw new RuntimeException("Strategy not found: " + signal.getStrategyId());
                }
                return strategy;
            }))
            .flatMap(strategy -> riskService.validateOrder(signal, strategy)
                .map(validatedSignal -> {
                    // Use validated signal and return strategy for next step
                    return strategy;
                })
            )
            .flatMap(strategy -> executeOptimizedOrder(signal, strategy, orderId))
            .doFinally(signalType -> latencyMonitor.completeLatencyTracking(orderId))
            .onErrorResume(error -> {
                logger.error("Signal execution failed for strategy {}: {}", signal.getStrategyId(), error.getMessage());
                return Mono.just(createErrorResponse(error.getMessage()));
            });
    }

    private Mono<Map<String, Object>> executeOptimizedOrder(SignalDto signal, StrategyInstance strategy, String orderId) {
        // Check if this is a multi-leg spread strategy
        if (isSpreadStrategy(signal)) {
            return executeSpreadOrder(signal, strategy, orderId);
        }
        
        // Single leg order with smart routing
        return executeSingleLegOrder(signal, strategy, orderId);
    }
    
    private Mono<Map<String, Object>> executeSingleLegOrder(SignalDto signal, StrategyInstance strategy, String orderId) {
        OptionContract contract = createOptionContract(signal);
        
        return latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.ORDER_ROUTING)
            .then(smartOrderRouter.getOptimalBroker(contract, signal.getType().toString(), signal.getQuantity().intValue()))
            .flatMap(optimalBroker -> {
                logger.info("Selected broker {} for order {}", optimalBroker.getBrokerName(), orderId);
                
                // Check if order needs slicing
                if (signal.getQuantity().intValue() > 100) {
                    return executeSlicedOrder(signal, strategy, orderId, optimalBroker);
                } else {
                    return executeDirectOrder(signal, strategy, orderId, optimalBroker);
                }
            });
    }
    
    private Mono<Map<String, Object>> executeSpreadOrder(SignalDto signal, StrategyInstance strategy, String orderId) {
        List<SpreadExecutionService.SpreadLeg> legs = createSpreadLegs(signal);
        double minCredit = calculateMinCredit(signal);
        
        return latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.ORDER_ROUTING)
            .then(spreadExecutionService.executeSpread(legs, minCredit))
            .flatMap(executionId -> {
                strategy.incrementExecutedTrades();
                
                Map<String, Object> result = new HashMap<>();
                result.put("strategyId", signal.getStrategyId());
                result.put("signal", signal);
                result.put("executionId", executionId);
                result.put("status", "spread_executed");
                result.put("orderId", orderId);
                
                return recordTCA(orderId, "SPREAD", signal, LocalDateTime.now(), LocalDateTime.now())
                    .then(Mono.just(result));
            });
    }
    
    private Mono<Map<String, Object>> executeSlicedOrder(SignalDto signal, StrategyInstance strategy, String orderId, BrokerEndpoint broker) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2); // 2-hour execution window
        
        // Use VWAP algorithm for large orders
        return vwapAlgorithm.sliceOrder(signal.getQuantity().intValue(), signal.getPrice().doubleValue(), startTime, endTime)
            .flatMap(slice -> executeOrderSlice(slice, signal, orderId, broker))
            .collectList()
            .map(sliceResults -> {
                strategy.incrementExecutedTrades();
                
                Map<String, Object> result = new HashMap<>();
                result.put("strategyId", signal.getStrategyId());
                result.put("signal", signal);
                result.put("sliceResults", sliceResults);
                result.put("status", "sliced_executed");
                result.put("orderId", orderId);
                
                return result;
            });
    }
    
    private Mono<Map<String, Object>> executeDirectOrder(SignalDto signal, StrategyInstance strategy, String orderId, BrokerEndpoint broker) {
        PlaceOrderDto orderDto = createOrderDto(signal, strategy);
        LocalDateTime signalTime = LocalDateTime.now();
        
        return latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.ORDER_SENT)
            .then(brokerService.placeOrder(orderDto))
            .flatMap(response -> latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.ORDER_ACK)
                .then(Mono.just(response)))
            .flatMap(response -> {
                strategy.incrementExecutedTrades();
                logger.info("Order placed for strategy {}: {} {} at {}", 
                    signal.getStrategyId(), signal.getType(), signal.getQuantity(), signal.getPrice());
                
                Map<String, Object> result = new HashMap<>();
                result.put("strategyId", signal.getStrategyId());
                result.put("signal", signal);
                result.put("orderResponse", response);
                result.put("status", "executed");
                result.put("orderId", orderId);
                result.put("broker", broker.getBrokerName());
                
                // Record TCA data
                return recordTCA(orderId, broker.getBrokerId(), signal, signalTime, LocalDateTime.now())
                    .then(latencyMonitor.recordStageLatency(orderId, LatencyMonitoringService.TradingStage.ORDER_FILL))
                    .then(Mono.just(result));
            });
    }

    private PlaceOrderDto createOrderDto(SignalDto signal, StrategyInstance strategy) {
        PlaceOrderDto orderDto = new PlaceOrderDto();
        
        // Set basic order parameters
        orderDto.setEs("nse_fo"); // Exchange segment
        orderDto.setPc("MIS"); // Product code
        orderDto.setTt(signal.getType() == SignalDto.SignalType.BUY ? "B" : "S"); // Transaction type
        orderDto.setPr(signal.getPrice().toString()); // Price
        orderDto.setQt(signal.getQuantity().toString()); // Quantity
        orderDto.setPt("L"); // Price type (Limit)
        orderDto.setRt("DAY"); // Retention type
        
        // Set symbol and other required fields
        orderDto.setTs(signal.getSymbol()); // Trading symbol
        orderDto.setAm("NO"); // After market order
        orderDto.setDq("0"); // Disclosed quantity
        orderDto.setMp("0"); // Market protection
        orderDto.setPf("N"); // Product flag
        orderDto.setTp("N"); // Trigger pending
        
        // Set stop loss and take profit if available
        if (signal.getStopLoss() != null) {
            orderDto.setSlt(signal.getStopLoss().toString());
        }
        if (signal.getTakeProfit() != null) {
            orderDto.setTlt(signal.getTakeProfit().toString());
        }
        
        return orderDto;
    }

    private Mono<Object> executeOrderSlice(OrderSlicingAlgorithm.OrderSlice slice, SignalDto signal, String orderId, BrokerEndpoint broker) {
        // Execute individual slice - simplified implementation
        return Mono.delay(java.time.Duration.between(LocalDateTime.now(), slice.getExecutionTime()))
            .then(Mono.fromCallable(() -> {
                Map<String, Object> sliceResult = new HashMap<>();
                sliceResult.put("quantity", slice.getQuantity());
                sliceResult.put("executionTime", slice.getExecutionTime());
                sliceResult.put("limitPrice", slice.getLimitPrice());
                sliceResult.put("status", "filled");
                return sliceResult;
            }));
    }
    
    private boolean isSpreadStrategy(SignalDto signal) {
        // Check if signal represents a multi-leg strategy
        return signal.getSymbol().contains("SPREAD") || signal.getSymbol().contains("IRON_CONDOR");
    }
    
    private OptionContract createOptionContract(SignalDto signal) {
        // Parse signal symbol to create option contract
        return new OptionContract(signal.getSymbol(), java.time.LocalDate.now().plusDays(30), 
                                signal.getPrice().doubleValue(), "CALL");
    }
    
    private List<SpreadExecutionService.SpreadLeg> createSpreadLegs(SignalDto signal) {
        // Create spread legs based on signal - simplified implementation
        return List.of(
            new SpreadExecutionService.SpreadLeg(signal.getSymbol() + "_LEG1", signal.getQuantity().intValue(), "SELL", signal.getPrice().doubleValue()),
            new SpreadExecutionService.SpreadLeg(signal.getSymbol() + "_LEG2", signal.getQuantity().intValue(), "BUY", signal.getPrice().doubleValue() * 0.95)
        );
    }
    
    private double calculateMinCredit(SignalDto signal) {
        return signal.getPrice().doubleValue() * 0.1; // 10% minimum credit
    }
    
    private Mono<Void> recordTCA(String orderId, String brokerId, SignalDto signal, LocalDateTime signalTime, LocalDateTime executionTime) {
        return tcaService.recordExecution(orderId, brokerId, signal.getPrice().doubleValue(), 
                                        signal.getPrice().doubleValue(), signal.getQuantity().intValue(), 
                                        5.0, signalTime, executionTime, "DIRECT");
    }
    
    private String generateOrderId() {
        return "ORD_" + System.currentTimeMillis();
    }
    
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", errorMessage);
        return errorResponse;
    }
}