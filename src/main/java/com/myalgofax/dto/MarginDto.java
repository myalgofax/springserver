package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarginDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    private String brkName;  // Broker Name
    private String brnchId;  // Branch ID
    private String exSeg;    // Exchange Segment
    private String prc;      // Price
    private String prcTp;    // Price Type
    private String prod;     // Product
    private String qty;      // Quantity
    private String tok;      // Token
    private String trnsTp;   // Transaction Type
    private String slAbsOrTks;    // Stop loss type (optional)
    private String slVal;         // Stop Loss Value (optional)
    private String sqrOffAbsOrTks; // Square Off type (optional)
    private String sqrOffVal;     // Square off value (optional)
    private String trailSL;       // Trailing Stop Loss (optional)
    private String trgPrc;        // Trigger price (optional)
    private String tSLTks;        // Trailing SL value (optional)

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

    public String getBrkName() {
        return brkName;
    }

    public void setBrkName(String brkName) {
        this.brkName = brkName;
    }

    public String getBrnchId() {
        return brnchId;
    }

    public void setBrnchId(String brnchId) {
        this.brnchId = brnchId;
    }

    public String getExSeg() {
        return exSeg;
    }

    public void setExSeg(String exSeg) {
        this.exSeg = exSeg;
    }

    public String getPrc() {
        return prc;
    }

    public void setPrc(String prc) {
        this.prc = prc;
    }

    public String getPrcTp() {
        return prcTp;
    }

    public void setPrcTp(String prcTp) {
        this.prcTp = prcTp;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getTok() {
        return tok;
    }

    public void setTok(String tok) {
        this.tok = tok;
    }

    public String getTrnsTp() {
        return trnsTp;
    }

    public void setTrnsTp(String trnsTp) {
        this.trnsTp = trnsTp;
    }

    public String getSlAbsOrTks() {
        return slAbsOrTks;
    }

    public void setSlAbsOrTks(String slAbsOrTks) {
        this.slAbsOrTks = slAbsOrTks;
    }

    public String getSlVal() {
        return slVal;
    }

    public void setSlVal(String slVal) {
        this.slVal = slVal;
    }

    public String getSqrOffAbsOrTks() {
        return sqrOffAbsOrTks;
    }

    public void setSqrOffAbsOrTks(String sqrOffAbsOrTks) {
        this.sqrOffAbsOrTks = sqrOffAbsOrTks;
    }

    public String getSqrOffVal() {
        return sqrOffVal;
    }

    public void setSqrOffVal(String sqrOffVal) {
        this.sqrOffVal = sqrOffVal;
    }

    public String getTrailSL() {
        return trailSL;
    }

    public void setTrailSL(String trailSL) {
        this.trailSL = trailSL;
    }

    public String getTrgPrc() {
        return trgPrc;
    }

    public void setTrgPrc(String trgPrc) {
        this.trgPrc = trgPrc;
    }

    public String getTSLTks() {
        return tSLTks;
    }

    public void setTSLTks(String tSLTks) {
        this.tSLTks = tSLTks;
    }
}