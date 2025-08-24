package com.myalgofax.ui.dto;

import java.time.Instant;
import java.util.Map;

public record PerformanceSnapshotDTO(
    Instant timestamp,
    double equity,
    double dailyPnl,
    Map<String, Double> strategyPerformance
) {}