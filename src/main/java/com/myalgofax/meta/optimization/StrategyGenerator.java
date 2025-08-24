package com.myalgofax.meta.optimization;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StrategyGenerator {
    
    private static final List<String> INDICATORS = Arrays.asList(
        "RSI", "MACD", "EMA", "SMA", "BOLLINGER_BANDS", "STOCHASTIC", "ATR", "VWAP"
    );
    
    private static final List<String> OPTION_STRUCTURES = Arrays.asList(
        "IRON_CONDOR", "BUTTERFLY", "STRADDLE", "STRANGLE", "COVERED_CALL", "PROTECTIVE_PUT"
    );
    
    private static final Map<String, List<Double>> PARAMETER_RANGES = Map.of(
        "RSI_PERIOD", Arrays.asList(10.0, 14.0, 20.0, 30.0),
        "RSI_OVERSOLD", Arrays.asList(20.0, 25.0, 30.0),
        "RSI_OVERBOUGHT", Arrays.asList(70.0, 75.0, 80.0),
        "EMA_FAST", Arrays.asList(5.0, 8.0, 12.0, 21.0),
        "EMA_SLOW", Arrays.asList(21.0, 34.0, 55.0, 89.0),
        "BOLLINGER_PERIOD", Arrays.asList(15.0, 20.0, 25.0),
        "BOLLINGER_STD", Arrays.asList(1.5, 2.0, 2.5)
    );
    
    public static class StrategyTemplate {
        private final String strategyId;
        private final List<String> indicators;
        private final String optionStructure;
        private final Map<String, Double> parameters;
        private final Map<String, String> entryConditions;
        private final Map<String, String> exitConditions;
        
        public StrategyTemplate(String strategyId, List<String> indicators, String optionStructure,
                              Map<String, Double> parameters, Map<String, String> entryConditions,
                              Map<String, String> exitConditions) {
            this.strategyId = strategyId;
            this.indicators = indicators;
            this.optionStructure = optionStructure;
            this.parameters = parameters;
            this.entryConditions = entryConditions;
            this.exitConditions = exitConditions;
        }
        
        // Getters
        public String getStrategyId() { return strategyId; }
        public List<String> getIndicators() { return indicators; }
        public String getOptionStructure() { return optionStructure; }
        public Map<String, Double> getParameters() { return parameters; }
        public Map<String, String> getEntryConditions() { return entryConditions; }
        public Map<String, String> getExitConditions() { return exitConditions; }
    }
    
    public static class BacktestResult {
        private final String strategyId;
        private final double totalReturn;
        private final double sharpeRatio;
        private final double maxDrawdown;
        private final int totalTrades;
        private final double winRate;
        private final boolean passedValidation;
        
        public BacktestResult(String strategyId, double totalReturn, double sharpeRatio,
                            double maxDrawdown, int totalTrades, double winRate, boolean passedValidation) {
            this.strategyId = strategyId;
            this.totalReturn = totalReturn;
            this.sharpeRatio = sharpeRatio;
            this.maxDrawdown = maxDrawdown;
            this.totalTrades = totalTrades;
            this.winRate = winRate;
            this.passedValidation = passedValidation;
        }
        
        // Getters
        public String getStrategyId() { return strategyId; }
        public double getTotalReturn() { return totalReturn; }
        public double getSharpeRatio() { return sharpeRatio; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public int getTotalTrades() { return totalTrades; }
        public double getWinRate() { return winRate; }
        public boolean isPassedValidation() { return passedValidation; }
    }
    
    public Flux<StrategyTemplate> generateStrategyCombinations(int maxCombinations) {
        return Flux.range(0, maxCombinations)
            .map(i -> generateRandomStrategy());
    }
    
    private StrategyTemplate generateRandomStrategy() {
        String strategyId = "GENERATED_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000);
        
        List<String> selectedIndicators = selectRandomIndicators();
        String optionStructure = OPTION_STRUCTURES.get(ThreadLocalRandom.current().nextInt(OPTION_STRUCTURES.size()));
        Map<String, Double> parameters = generateRandomParameters(selectedIndicators);
        Map<String, String> entryConditions = generateEntryConditions(selectedIndicators, parameters);
        Map<String, String> exitConditions = generateExitConditions(optionStructure);
        
        return new StrategyTemplate(strategyId, selectedIndicators, optionStructure, 
                                  parameters, entryConditions, exitConditions);
    }
    
    private List<String> selectRandomIndicators() {
        int numIndicators = ThreadLocalRandom.current().nextInt(2, 5);
        List<String> selected = new ArrayList<>();
        List<String> available = new ArrayList<>(INDICATORS);
        
        for (int i = 0; i < numIndicators && !available.isEmpty(); i++) {
            int index = ThreadLocalRandom.current().nextInt(available.size());
            selected.add(available.remove(index));
        }
        
        return selected;
    }
    
    private Map<String, Double> generateRandomParameters(List<String> indicators) {
        Map<String, Double> parameters = new HashMap<>();
        
        for (String indicator : indicators) {
            switch (indicator) {
                case "RSI":
                    parameters.put("RSI_PERIOD", selectRandomValue("RSI_PERIOD"));
                    parameters.put("RSI_OVERSOLD", selectRandomValue("RSI_OVERSOLD"));
                    parameters.put("RSI_OVERBOUGHT", selectRandomValue("RSI_OVERBOUGHT"));
                    break;
                case "EMA":
                    parameters.put("EMA_FAST", selectRandomValue("EMA_FAST"));
                    parameters.put("EMA_SLOW", selectRandomValue("EMA_SLOW"));
                    break;
                case "BOLLINGER_BANDS":
                    parameters.put("BOLLINGER_PERIOD", selectRandomValue("BOLLINGER_PERIOD"));
                    parameters.put("BOLLINGER_STD", selectRandomValue("BOLLINGER_STD"));
                    break;
            }
        }
        
        return parameters;
    }
    
    private Double selectRandomValue(String parameterName) {
        List<Double> values = PARAMETER_RANGES.get(parameterName);
        if (values == null || values.isEmpty()) {
            return 14.0;
        }
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
    
    private Map<String, String> generateEntryConditions(List<String> indicators, Map<String, Double> parameters) {
        Map<String, String> conditions = new HashMap<>();
        
        for (String indicator : indicators) {
            switch (indicator) {
                case "RSI":
                    conditions.put("RSI_ENTRY", String.format("RSI < %.0f OR RSI > %.0f", 
                        parameters.get("RSI_OVERSOLD"), parameters.get("RSI_OVERBOUGHT")));
                    break;
                case "EMA":
                    conditions.put("EMA_ENTRY", "EMA_FAST > EMA_SLOW");
                    break;
                case "MACD":
                    conditions.put("MACD_ENTRY", "MACD_LINE > SIGNAL_LINE");
                    break;
            }
        }
        
        return conditions;
    }
    
    private Map<String, String> generateExitConditions(String optionStructure) {
        Map<String, String> conditions = new HashMap<>();
        
        switch (optionStructure) {
            case "IRON_CONDOR":
                conditions.put("PROFIT_TARGET", "PNL > 0.5 * MAX_PROFIT");
                conditions.put("STOP_LOSS", "PNL < -2.0 * CREDIT_RECEIVED");
                conditions.put("TIME_DECAY", "DTE < 7");
                break;
            case "STRADDLE":
                conditions.put("PROFIT_TARGET", "PNL > 0.3 * PREMIUM_PAID");
                conditions.put("STOP_LOSS", "PNL < -0.5 * PREMIUM_PAID");
                break;
        }
        
        return conditions;
    }
    
    public Mono<BacktestResult> backtestStrategy(StrategyTemplate strategy, int backtestDays) {
        return Mono.fromCallable(() -> {
            double totalReturn = simulateBacktest(strategy, backtestDays);
            double sharpeRatio = calculateSharpeRatio(totalReturn, backtestDays);
            double maxDrawdown = simulateMaxDrawdown();
            int totalTrades = simulateTotalTrades(backtestDays);
            double winRate = simulateWinRate();
            
            boolean passedValidation = validateStrategy(totalReturn, sharpeRatio, maxDrawdown, totalTrades, winRate);
            
            return new BacktestResult(strategy.getStrategyId(), totalReturn, sharpeRatio, 
                                    maxDrawdown, totalTrades, winRate, passedValidation);
        });
    }
    
    private double simulateBacktest(StrategyTemplate strategy, int days) {
        Random random = new Random(strategy.getStrategyId().hashCode());
        
        double totalReturn = 0.0;
        for (int i = 0; i < days; i++) {
            double dailyReturn = (random.nextGaussian() * 0.02) + getStrategyBias(strategy);
            totalReturn += dailyReturn;
        }
        
        return totalReturn;
    }
    
    private double getStrategyBias(StrategyTemplate strategy) {
        double bias = 0.0;
        
        if (strategy.getIndicators().contains("RSI") && strategy.getIndicators().contains("MACD")) {
            bias += 0.0005;
        }
        
        if (strategy.getOptionStructure().equals("IRON_CONDOR")) {
            bias += 0.0003;
        }
        
        return bias;
    }
    
    private double calculateSharpeRatio(double totalReturn, int days) {
        double annualizedReturn = totalReturn * (252.0 / days);
        double volatility = 0.15 + Math.random() * 0.1;
        return (annualizedReturn - 0.03) / volatility;
    }
    
    private double simulateMaxDrawdown() {
        return Math.random() * 0.2;
    }
    
    private int simulateTotalTrades(int days) {
        return (int) (days * (0.1 + Math.random() * 0.3));
    }
    
    private double simulateWinRate() {
        return 0.4 + Math.random() * 0.4;
    }
    
    private boolean validateStrategy(double totalReturn, double sharpeRatio, double maxDrawdown, 
                                   int totalTrades, double winRate) {
        return sharpeRatio > 1.0 &&
               maxDrawdown < 0.15 &&
               totalTrades > 20 &&
               winRate > 0.45 &&
               totalReturn > 0.05;
    }
    
    public Flux<StrategyTemplate> generateAndTestStrategies(int numStrategies, int backtestDays) {
        return generateStrategyCombinations(numStrategies)
            .flatMap(strategy -> backtestStrategy(strategy, backtestDays)
                .filter(BacktestResult::isPassedValidation)
                .map(result -> strategy))
            .take(10);
    }
}