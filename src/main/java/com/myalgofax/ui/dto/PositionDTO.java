package com.myalgofax.ui.dto;

import java.time.LocalDateTime;

public record PositionDTO(
    String symbol,
    int quantity,
    double avgPrice,
    double currentPrice,
    double unrealizedPnl,
    LocalDateTime openedAt
) {}