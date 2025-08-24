package com.myalgofax.execution.algorithms;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class ImplementationShortfallAlgorithm implements OrderSlicingAlgorithm {
    
    private static final double ALPHA_DECAY_RATE = 0.1; // Signal decay per hour
    private static final double VOLATILITY_THRESHOLD = 0.02; // 2% volatility threshold
    
    @Override
    public Flux<OrderSlice> sliceOrder(int totalQuantity, double currentPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Duration totalDuration = Duration.between(startTime, endTime);
        double totalHours = totalDuration.toMinutes() / 60.0;
        
        // Calculate optimal participation rate based on signal strength and market impact
        double participationRate = calculateOptimalParticipationRate(totalQuantity, currentPrice, totalHours);
        
        int intervals = Math.max(1, (int) (totalHours * 4)); // 15-minute intervals
        
        return Flux.range(0, intervals)
            .map(i -> {
                double timeProgress = (double) i / intervals;
                double remainingAlpha = Math.exp(-ALPHA_DECAY_RATE * timeProgress * totalHours);
                
                // Adjust aggressiveness based on remaining alpha
                double aggressiveness = Math.min(1.0, remainingAlpha * participationRate);
                int sliceQuantity = (int) (totalQuantity * aggressiveness / intervals);
                
                LocalDateTime executionTime = startTime.plusMinutes(i * totalDuration.toMinutes() / intervals);
                
                // Adjust price based on urgency
                double priceAdjustment = aggressiveness * 0.001; // 0.1% max adjustment
                double limitPrice = currentPrice * (1 + priceAdjustment);
                
                return new OrderSlice(sliceQuantity, executionTime, limitPrice);
            })
            .filter(slice -> slice.getQuantity() > 0);
    }
    
    private double calculateOptimalParticipationRate(int quantity, double price, double hours) {
        // Simplified implementation shortfall model
        double marketImpact = Math.sqrt(quantity * price) / 10000.0; // Simplified impact model
        double opportunityCost = ALPHA_DECAY_RATE * hours;
        
        // Balance market impact vs opportunity cost
        return Math.min(0.3, 1.0 / (1.0 + marketImpact + opportunityCost));
    }
}