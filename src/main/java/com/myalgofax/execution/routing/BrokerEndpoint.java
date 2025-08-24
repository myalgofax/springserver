package com.myalgofax.execution.routing;

import java.time.LocalDateTime;

public class BrokerEndpoint {
    private String brokerId;
    private String brokerName;
    private double latencyMs;
    private double uptimePercentage;
    private double fillRate;
    private double feeStructure;
    private LocalDateTime lastHealthCheck;
    
    public BrokerEndpoint(String brokerId, String brokerName) {
        this.brokerId = brokerId;
        this.brokerName = brokerName;
        this.lastHealthCheck = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getBrokerId() { return brokerId; }
    public void setBrokerId(String brokerId) { this.brokerId = brokerId; }
    
    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }
    
    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }
    
    public double getUptimePercentage() { return uptimePercentage; }
    public void setUptimePercentage(double uptimePercentage) { this.uptimePercentage = uptimePercentage; }
    
    public double getFillRate() { return fillRate; }
    public void setFillRate(double fillRate) { this.fillRate = fillRate; }
    
    public double getFeeStructure() { return feeStructure; }
    public void setFeeStructure(double feeStructure) { this.feeStructure = feeStructure; }
    
    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
}