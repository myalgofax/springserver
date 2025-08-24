package com.myalgofax.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TechnicalIndicatorService {
    
    private final Map<String, LinkedList<BigDecimal>> priceHistory = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<BigDecimal>> volumeHistory = new ConcurrentHashMap<>();
    
    public void updatePrice(String symbol, BigDecimal price, BigDecimal volume) {
        priceHistory.computeIfAbsent(symbol, k -> new LinkedList<>()).addLast(price);
        volumeHistory.computeIfAbsent(symbol, k -> new LinkedList<>()).addLast(volume);
        
        // Keep only last 200 data points for performance
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices.size() > 200) {
            prices.removeFirst();
        }
        
        LinkedList<BigDecimal> volumes = volumeHistory.get(symbol);
        if (volumes.size() > 200) {
            volumes.removeFirst();
        }
    }

    public BigDecimal calculateSMA(String symbol, int period) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period) {
            return null;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i));
        }
        
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEMA(String symbol, int period) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period) {
            return null;
        }
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = prices.get(prices.size() - period);
        
        for (int i = prices.size() - period + 1; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRSI(String symbol, int period) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period + 1) {
            return null;
        }
        
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        
        avgGain = avgGain.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP));
    }

    public Map<String, BigDecimal> calculateMACD(String symbol) {
        BigDecimal ema12 = calculateEMA(symbol, 12);
        BigDecimal ema26 = calculateEMA(symbol, 26);
        
        if (ema12 == null || ema26 == null) {
            return null;
        }
        
        BigDecimal macdLine = ema12.subtract(ema26);
        BigDecimal signalLine = calculateEMAFromValue(macdLine, 9); // Simplified signal line
        
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("macd", macdLine);
        result.put("signal", signalLine != null ? signalLine : macdLine);
        result.put("histogram", macdLine.subtract(signalLine != null ? signalLine : BigDecimal.ZERO));
        
        return result;
    }

    public Map<String, BigDecimal> calculateBollingerBands(String symbol, int period, double stdDev) {
        BigDecimal sma = calculateSMA(symbol, period);
        if (sma == null) {
            return null;
        }
        
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        BigDecimal variance = BigDecimal.ZERO;
        
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal diff = prices.get(i).subtract(sma);
            variance = variance.add(diff.multiply(diff));
        }
        
        variance = variance.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal bandWidth = standardDeviation.multiply(BigDecimal.valueOf(stdDev));
        
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("upper", sma.add(bandWidth));
        result.put("middle", sma);
        result.put("lower", sma.subtract(bandWidth));
        
        return result;
    }

    public BigDecimal getResistanceLevel(String symbol, int period) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period) {
            return null;
        }
        
        BigDecimal max = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            if (prices.get(i).compareTo(max) > 0) {
                max = prices.get(i);
            }
        }
        
        return max;
    }

    public BigDecimal getSupportLevel(String symbol, int period) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period) {
            return null;
        }
        
        BigDecimal min = prices.get(prices.size() - 1);
        for (int i = prices.size() - period; i < prices.size(); i++) {
            if (prices.get(i).compareTo(min) < 0) {
                min = prices.get(i);
            }
        }
        
        return min;
    }

    public BigDecimal calculateEMA(String symbol, int period, int offset) {
        LinkedList<BigDecimal> prices = priceHistory.get(symbol);
        if (prices == null || prices.size() < period + offset) {
            return null;
        }
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        int startIndex = prices.size() - period - offset;
        int endIndex = prices.size() - offset;
        
        BigDecimal ema = prices.get(startIndex);
        
        for (int i = startIndex + 1; i < endIndex; i++) {
            ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEMAFromValue(BigDecimal value, int period) {
        // Simplified EMA calculation for signal line
        return value.multiply(BigDecimal.valueOf(0.8));
    }
}