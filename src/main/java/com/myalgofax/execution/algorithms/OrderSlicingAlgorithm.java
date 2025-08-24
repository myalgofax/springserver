package com.myalgofax.execution.algorithms;

import reactor.core.publisher.Flux;
import java.time.LocalDateTime;

public interface OrderSlicingAlgorithm {
    
    class OrderSlice {
        private final int quantity;
        private final LocalDateTime executionTime;
        private final double limitPrice;
        
        public OrderSlice(int quantity, LocalDateTime executionTime, double limitPrice) {
            this.quantity = quantity;
            this.executionTime = executionTime;
            this.limitPrice = limitPrice;
        }
        
        public int getQuantity() { return quantity; }
        public LocalDateTime getExecutionTime() { return executionTime; }
        public double getLimitPrice() { return limitPrice; }
    }
    
    Flux<OrderSlice> sliceOrder(int totalQuantity, double currentPrice, LocalDateTime startTime, LocalDateTime endTime);
}