package com.myalgofax.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.myalgofax.dto.MarketDataDto;
import com.myalgofax.dto.SignalDto;
import com.myalgofax.service.PortfolioService;
import com.myalgofax.service.PortfolioService.Position;

import reactor.core.publisher.Mono;

@Component
public class EMACrossoverWithRSIStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(EMACrossoverWithRSIStrategy.class);
    
    private final TechnicalIndicatorService indicatorService;
    private final PortfolioService portfolioService;
    
    public EMACrossoverWithRSIStrategy(TechnicalIndicatorService indicatorService, PortfolioService portfolioService) {
        this.indicatorService = indicatorService;
        this.portfolioService = portfolioService;
    }

    /**
     * Evaluates EMA Crossover with RSI Filter strategy for given market data
     * @param data Current market data with OHLCV
     * @param parameters Strategy configuration parameters
     * @return Mono<SignalDto> containing trading signal or empty if no signal
     */
    public Mono<SignalDto> evaluate(MarketDataDto data, Map<String, Object> parameters) {
        return Mono.fromCallable(() -> extractParameters(parameters))
            .flatMap(params -> evaluateStrategy(data, params))
            .onErrorResume(error -> {
                logger.error("Strategy evaluation failed for {}: {}", data.getSymbol(), error.getMessage());
                return Mono.empty();
            });
    }

    private Mono<SignalDto> evaluateStrategy(MarketDataDto data, StrategyParams params) {
        String symbol = data.getSymbol();
        
        return Mono.zip(
            getCurrentIndicators(symbol, params),
            getPreviousIndicators(symbol, params),
            portfolioService.getPosition(symbol)
        ).flatMap(tuple -> {
            Indicators current = tuple.getT1();
            Indicators previous = tuple.getT2();
            Position position = tuple.getT3();
            
            // Check exit conditions first if position exists
            if (position != null && position.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                return checkExitConditions(data, current, position, params);
            }
            
            // Check entry conditions if no position
            return checkEntryConditions(data, current, previous, params);
        });
    }

    private Mono<SignalDto> checkEntryConditions(MarketDataDto data, Indicators current, Indicators previous, StrategyParams params) {
        // Entry Logic: EMA Crossover + RSI Filter + Volume Confirmation
        boolean bullishCrossover = current.fastEma.compareTo(current.slowEma) > 0;
        boolean crossJustHappened = previous.fastEma.compareTo(previous.slowEma) <= 0;
        boolean rsiHealthy = current.rsi.compareTo(BigDecimal.valueOf(50)) > 0 && 
                           current.rsi.compareTo(BigDecimal.valueOf(70)) < 0;
        boolean volumeConfirms = data.getVolume().compareTo(current.volumeAverage) > 0;
        
        if (bullishCrossover && crossJustHappened && rsiHealthy && volumeConfirms) {
            return generateBuySignal(data, current, params);
        }
        
        return Mono.empty();
    }

    private Mono<SignalDto> checkExitConditions(MarketDataDto data, Indicators current, Position position, StrategyParams params) {
        BigDecimal currentPrice = data.getPrice();
        BigDecimal entryPrice = position.getEntryPrice();
        BigDecimal stopLoss = position.getStopLoss();
        BigDecimal takeProfit = position.getTakeProfit();
        
        // Exit conditions
        boolean takeProfitHit = currentPrice.compareTo(takeProfit) >= 0;
        boolean stopLossHit = currentPrice.compareTo(stopLoss) <= 0;
        boolean trendReversal = current.fastEma.compareTo(current.slowEma) < 0;
        
        if (takeProfitHit || stopLossHit || trendReversal) {
            String reason = takeProfitHit ? "Take Profit" : 
                          stopLossHit ? "Stop Loss" : "Trend Reversal";
            return generateSellSignal(data, position, reason);
        }
        
        return Mono.empty();
    }

    private Mono<SignalDto> generateBuySignal(MarketDataDto data, Indicators indicators, StrategyParams params) {
        return Mono.fromCallable(() -> {
            BigDecimal entryPrice = data.getPrice();
            BigDecimal atrValue = indicators.atr;
            BigDecimal stopLoss = entryPrice.subtract(atrValue);
            BigDecimal takeProfit = entryPrice.add(atrValue.multiply(BigDecimal.valueOf(params.riskRewardRatio)));
            
            // Calculate position size based on risk (simplified)
            BigDecimal riskAmount = entryPrice.subtract(stopLoss);
            BigDecimal recommendedQuantity = BigDecimal.valueOf(1000).divide(riskAmount, 2, RoundingMode.HALF_UP);
            
            SignalDto signal = new SignalDto();
            signal.setType(SignalDto.SignalType.BUY);
            signal.setSymbol(data.getSymbol());
            signal.setPrice(entryPrice);
            signal.setQuantity(recommendedQuantity);
            signal.setStopLoss(stopLoss);
            signal.setTakeProfit(takeProfit);
            signal.setReason("EMA Bullish Crossover with RSI confirmation");
            
            logger.info("BUY signal generated for {}: Entry={}, SL={}, TP={}", 
                data.getSymbol(), entryPrice, stopLoss, takeProfit);
            
            return signal;
        });
    }

    private Mono<SignalDto> generateSellSignal(MarketDataDto data, Position position, String reason) {
        return Mono.fromCallable(() -> {
            SignalDto signal = new SignalDto();
            signal.setType(SignalDto.SignalType.SELL);
            signal.setSymbol(data.getSymbol());
            signal.setPrice(data.getPrice());
            signal.setQuantity(position.getQuantity());
            signal.setReason(reason);
            
            logger.info("SELL signal generated for {}: {} at {}", 
                data.getSymbol(), reason, data.getPrice());
            
            return signal;
        });
    }

    private Mono<Indicators> getCurrentIndicators(String symbol, StrategyParams params) {
        return Mono.zip(
            Mono.fromCallable(() -> indicatorService.calculateEMA(symbol, params.fastEmaPeriod)),
            Mono.fromCallable(() -> indicatorService.calculateEMA(symbol, params.slowEmaPeriod)),
            Mono.fromCallable(() -> indicatorService.calculateRSI(symbol, params.rsiPeriod)),
            Mono.fromCallable(() -> calculateATR(symbol, params.atrPeriod)),
            Mono.fromCallable(() -> calculateVolumeAverage(symbol, params.volumePeriod))
        ).map(tuple -> new Indicators(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()));
    }

    private Mono<Indicators> getPreviousIndicators(String symbol, StrategyParams params) {
        // Get previous period indicators for crossover detection
        return Mono.zip(
            Mono.fromCallable(() -> indicatorService.calculateEMA(symbol, params.fastEmaPeriod, 1)), // 1 period back
            Mono.fromCallable(() -> indicatorService.calculateEMA(symbol, params.slowEmaPeriod, 1)),
            Mono.fromCallable(() -> BigDecimal.ZERO), // RSI not needed for previous
            Mono.fromCallable(() -> BigDecimal.ZERO), // ATR not needed for previous
            Mono.fromCallable(() -> BigDecimal.ZERO)  // Volume not needed for previous
        ).map(tuple -> new Indicators(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()));
    }

    private BigDecimal calculateATR(String symbol, int period) {
        // Simplified ATR calculation - should use proper True Range calculation
        return indicatorService.calculateSMA(symbol, period).multiply(BigDecimal.valueOf(0.02)); // 2% of price as ATR approximation
    }

    private BigDecimal calculateVolumeAverage(String symbol, int period) {
        // Get volume average from indicator service
        return indicatorService.calculateSMA(symbol + "_VOLUME", period);
    }

    private StrategyParams extractParameters(Map<String, Object> parameters) {
        return new StrategyParams(
            (String) parameters.get("symbol"),
            (Integer) parameters.getOrDefault("fastEmaPeriod", 9),
            (Integer) parameters.getOrDefault("slowEmaPeriod", 21),
            (Integer) parameters.getOrDefault("rsiPeriod", 14),
            (Integer) parameters.getOrDefault("atrPeriod", 14),
            ((Number) parameters.getOrDefault("riskRewardRatio", 2.0)).doubleValue(),
            (Integer) parameters.getOrDefault("volumePeriod", 20)
        );
    }

    // Inner classes for data structures
    private static class StrategyParams {
        final String symbol;
        final int fastEmaPeriod;
        final int slowEmaPeriod;
        final int rsiPeriod;
        final int atrPeriod;
        final double riskRewardRatio;
        final int volumePeriod;

        StrategyParams(String symbol, int fastEmaPeriod, int slowEmaPeriod, int rsiPeriod, 
                      int atrPeriod, double riskRewardRatio, int volumePeriod) {
            this.symbol = symbol;
            this.fastEmaPeriod = fastEmaPeriod;
            this.slowEmaPeriod = slowEmaPeriod;
            this.rsiPeriod = rsiPeriod;
            this.atrPeriod = atrPeriod;
            this.riskRewardRatio = riskRewardRatio;
            this.volumePeriod = volumePeriod;
        }
    }

    private static class Indicators {
        final BigDecimal fastEma;
        final BigDecimal slowEma;
        final BigDecimal rsi;
        final BigDecimal atr;
        final BigDecimal volumeAverage;

        Indicators(BigDecimal fastEma, BigDecimal slowEma, BigDecimal rsi, BigDecimal atr, BigDecimal volumeAverage) {
            this.fastEma = fastEma;
            this.slowEma = slowEma;
            this.rsi = rsi;
            this.atr = atr;
            this.volumeAverage = volumeAverage;
        }
    }


}