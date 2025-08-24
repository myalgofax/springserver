package com.myalgofax.ui.dto;

import java.util.Map;

public record StrategyConfig(
    String strategyType,
    String symbol,
    Map<String, Object> parameters,
    double maxCapital,
    double riskLimit
) {}