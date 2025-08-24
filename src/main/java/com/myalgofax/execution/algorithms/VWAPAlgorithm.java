package com.myalgofax.execution.algorithms;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class VWAPAlgorithm implements OrderSlicingAlgorithm {
    
    // Typical intraday volume distribution (percentage of daily volume by hour)
    private static final List<Double> VOLUME_CURVE = Arrays.asList(
        0.02, 0.03, 0.04, 0.06, 0.08, 0.12, 0.15, 0.18, 0.16, 0.10, 0.04, 0.02
    );
    
    @Override
    public Flux<OrderSlice> sliceOrder(int totalQuantity, double currentPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Duration totalDuration = Duration.between(startTime, endTime);
        long totalMinutes = totalDuration.toMinutes();
        int intervals = Math.min(12, (int) (totalMinutes / 30)); // 30-minute intervals, max 12
        
        return Flux.range(0, intervals)
            .map(i -> {
                double volumeWeight = getVolumeWeight(i, intervals);
                int sliceQuantity = (int) (totalQuantity * volumeWeight);
                LocalDateTime executionTime = startTime.plusMinutes(i * totalMinutes / intervals);
                
                return new OrderSlice(sliceQuantity, executionTime, currentPrice);
            })
            .filter(slice -> slice.getQuantity() > 0);
    }
    
    private double getVolumeWeight(int interval, int totalIntervals) {
        if (totalIntervals <= VOLUME_CURVE.size()) {
            return VOLUME_CURVE.get(interval * VOLUME_CURVE.size() / totalIntervals);
        }
        
        // For longer periods, distribute more evenly
        return 1.0 / totalIntervals;
    }
}