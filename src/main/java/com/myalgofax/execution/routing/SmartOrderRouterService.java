package com.myalgofax.execution.routing;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class SmartOrderRouterService {
    
    private final Map<String, BrokerEndpoint> brokerEndpoints = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> liquidityData = new ConcurrentHashMap<>();
    
    public SmartOrderRouterService() {
        initializeBrokers();
    }
    
    private void initializeBrokers() {
        brokerEndpoints.put("ZERODHA", new BrokerEndpoint("ZERODHA", "Zerodha"));
        brokerEndpoints.put("KOTAK", new BrokerEndpoint("KOTAK", "Kotak Securities"));
    }
    
    public Mono<BrokerEndpoint> getOptimalBroker(OptionContract contract, String orderType, int quantity) {
        return Flux.fromIterable(brokerEndpoints.values())
            .filter(broker -> isHealthy(broker))
            .collectList()
            .flatMap(healthyBrokers -> {
                if (healthyBrokers.isEmpty()) {
                    return Mono.error(new RuntimeException("No healthy brokers available"));
                }
                
                return Mono.fromCallable(() -> selectBestBroker(healthyBrokers, contract, orderType, quantity));
            });
    }
    
    private boolean isHealthy(BrokerEndpoint broker) {
        return broker.getUptimePercentage() > 95.0 && 
               broker.getLatencyMs() < 100.0 &&
               Duration.between(broker.getLastHealthCheck(), LocalDateTime.now()).toMinutes() < 5;
    }
    
    private BrokerEndpoint selectBestBroker(List<BrokerEndpoint> brokers, OptionContract contract, String orderType, int quantity) {
        return brokers.stream()
            .max(Comparator.comparingDouble(broker -> calculateBrokerScore(broker, contract, orderType, quantity)))
            .orElse(brokers.get(0));
    }
    
    private double calculateBrokerScore(BrokerEndpoint broker, OptionContract contract, String orderType, int quantity) {
        double latencyScore = Math.max(0, 100 - broker.getLatencyMs()) / 100.0;
        double fillRateScore = broker.getFillRate();
        double feeScore = Math.max(0, 100 - broker.getFeeStructure()) / 100.0;
        double liquidityScore = getLiquidityScore(broker.getBrokerId(), contract.toString());
        
        // Weighted scoring
        return (latencyScore * 0.3) + (fillRateScore * 0.4) + (feeScore * 0.2) + (liquidityScore * 0.1);
    }
    
    private double getLiquidityScore(String brokerId, String contractId) {
        return liquidityData.getOrDefault(brokerId, Collections.emptyMap())
                          .getOrDefault(contractId, 0.5); // Default neutral score
    }
    
    public void updateBrokerMetrics(String brokerId, double latency, double uptime, double fillRate) {
        BrokerEndpoint broker = brokerEndpoints.get(brokerId);
        if (broker != null) {
            broker.setLatencyMs(latency);
            broker.setUptimePercentage(uptime);
            broker.setFillRate(fillRate);
            broker.setLastHealthCheck(LocalDateTime.now());
        }
    }
    
    public void updateLiquidityData(String brokerId, String contractId, double depth) {
        liquidityData.computeIfAbsent(brokerId, k -> new ConcurrentHashMap<>())
                   .put(contractId, Math.min(1.0, depth / 1000.0)); // Normalize depth
    }
}