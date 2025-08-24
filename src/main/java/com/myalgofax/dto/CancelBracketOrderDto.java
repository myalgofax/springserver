package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelBracketOrderDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    private String am;  // After Market
    private String on;  // Order Number

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

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
    }

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }
}