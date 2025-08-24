package com.myalgofax.execution.algorithms;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class TWAPAlgorithm implements OrderSlicingAlgorithm {
    
    @Override
    public Flux<OrderSlice> sliceOrder(int totalQuantity, double currentPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Duration totalDuration = Duration.between(startTime, endTime);
        long totalMinutes = totalDuration.toMinutes();
        int intervals = Math.max(1, Math.min(20, (int) (totalMinutes / 15))); // 15-minute intervals, max 20
        
        int baseQuantity = totalQuantity / intervals;
        int remainder = totalQuantity % intervals;
        
        return Flux.range(0, intervals)
            .map(i -> {
                int sliceQuantity = baseQuantity + (i < remainder ? 1 : 0);
                LocalDateTime executionTime = startTime.plusMinutes(i * totalMinutes / intervals);
                
                return new OrderSlice(sliceQuantity, executionTime, currentPrice);
            });
    }
}