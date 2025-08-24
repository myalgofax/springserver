package com.myalgofax.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.myalgofax.dto.SignalDto;
import com.myalgofax.strategy.StrategyInstance;

import reactor.core.publisher.Mono;

@Service
public class RiskManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskManagementService.class);
    
    private final Map<String, BigDecimal> userDailyPnL = new ConcurrentHashMap<>();
    private final Map<String, Integer> userDailyTrades = new ConcurrentHashMap<>();
    
    // Risk limits
    private static final BigDecimal MAX_DAILY_LOSS = BigDecimal.valueOf(-10000);
    private static final BigDecimal MAX_POSITION_SIZE = BigDecimal.valueOf(100000);
    private static final int MAX_DAILY_TRADES = 100;
    private static final BigDecimal MAX_SINGLE_ORDER_SIZE = BigDecimal.valueOf(10000);

    public Mono<SignalDto> validateOrder(SignalDto signal, StrategyInstance strategy) {
        return Mono.fromCallable(() -> {
            String userId = strategy.getUserId();
            
            // Check daily loss limit
            BigDecimal dailyPnL = userDailyPnL.getOrDefault(userId, BigDecimal.ZERO);
            if (dailyPnL.compareTo(MAX_DAILY_LOSS) <= 0) {
                throw new RuntimeException("Daily loss limit exceeded for user: " + userId);
            }
            
            // Check daily trade limit
            int dailyTrades = userDailyTrades.getOrDefault(userId, 0);
            if (dailyTrades >= MAX_DAILY_TRADES) {
                throw new RuntimeException("Daily trade limit exceeded for user: " + userId);
            }
            
            // Check single order size
            BigDecimal orderValue = signal.getPrice().multiply(signal.getQuantity());
            if (orderValue.compareTo(MAX_SINGLE_ORDER_SIZE) > 0) {
                throw new RuntimeException("Order size exceeds maximum allowed: " + orderValue);
            }
            
            // Check position size limits
            if (orderValue.compareTo(MAX_POSITION_SIZE) > 0) {
                throw new RuntimeException("Position size exceeds maximum allowed: " + orderValue);
            }
            
            // Validate strategy-specific risk parameters
            validateStrategyRisk(signal, strategy);
            
            logger.info("Risk validation passed for signal: {} {}", signal.getType(), signal.getSymbol());
            return signal;
        });
    }

    private void validateStrategyRisk(SignalDto signal, StrategyInstance strategy) {
        Map<String, Object> params = strategy.getParameters();
        
        // Check if strategy has risk parameters
        if (params.containsKey("maxLoss")) {
            BigDecimal maxLoss = new BigDecimal(params.get("maxLoss").toString());
            if (strategy.getTotalPnL().compareTo(maxLoss.negate()) <= 0) {
                throw new RuntimeException("Strategy maximum loss exceeded: " + strategy.getStrategyId());
            }
        }
        
        if (params.containsKey("maxTrades")) {
            int maxTrades = (Integer) params.get("maxTrades");
            if (strategy.getExecutedTrades() >= maxTrades) {
                throw new RuntimeException("Strategy maximum trades exceeded: " + strategy.getStrategyId());
            }
        }
    }

    public void updateUserPnL(String userId, BigDecimal pnl) {
        userDailyPnL.merge(userId, pnl, BigDecimal::add);
    }

    public void incrementUserTrades(String userId) {
        userDailyTrades.merge(userId, 1, Integer::sum);
    }

    public BigDecimal getUserDailyPnL(String userId) {
        return userDailyPnL.getOrDefault(userId, BigDecimal.ZERO);
    }

    public int getUserDailyTrades(String userId) {
        return userDailyTrades.getOrDefault(userId, 0);
    }

    public boolean isCircuitBreakerTriggered(String userId) {
        BigDecimal dailyPnL = getUserDailyPnL(userId);
        return dailyPnL.compareTo(MAX_DAILY_LOSS) <= 0;
    }

    public void resetDailyLimits() {
        userDailyPnL.clear();
        userDailyTrades.clear();
        logger.info("Daily risk limits reset");
    }
}