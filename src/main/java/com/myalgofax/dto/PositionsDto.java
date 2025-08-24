package com.myalgofax.dto;

public class PositionsDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    
    public String getAccessBrokerToken() {
        return accessBrokerToken;
    }
    
    public void setAccessBrokerToken(String accessBrokerToken) {
        this.accessBrokerToken = accessBrokerToken;
    }
    
    public String getBrokerCode() {
        return brokerCode;
    }
    
    public void setBrokerCode(String brokerCode) {
        this.brokerCode = brokerCode;
    }
    
    @Override
    public String toString() {
        return "PositionsDto [accessBrokerToken=" + accessBrokerToken + ", brokerCode=" + brokerCode + "]";
    }
}