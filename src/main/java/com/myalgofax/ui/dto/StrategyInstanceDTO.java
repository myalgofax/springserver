package com.myalgofax.ui.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

public record StrategyInstanceDTO(
    String id,
    String name,
    String symbol,
    StrategyStatus status,
    double currentPnl,
    double unrealizedPnl,
    double realizedPnl,
    LocalDateTime startedAt,
    Map<String, Double> currentGreeks,
    StrategyConfig config,
    List<PositionDTO> openPositions
) {
    public enum StrategyStatus {
        RUNNING, PAUSED, STOPPED, ERROR
    }
}