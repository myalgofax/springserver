package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitsDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    private String seg;   // Segment
    private String exch;  // Exchange
    private String prod;  // Product

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

    public String getSeg() {
        return seg;
    }

    public void setSeg(String seg) {
        this.seg = seg;
    }

    public String getExch() {
        return exch;
    }

    public void setExch(String exch) {
        this.exch = exch;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }
}