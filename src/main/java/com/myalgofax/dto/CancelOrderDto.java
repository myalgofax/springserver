package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelOrderDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    private String on;  // Order Number
    private String am;  // AMO (optional)
    private String ts;  // Trading Symbol (optional)

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

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}