package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifyOrderDto {
    
    private String accessBrokerToken;
    private String brokerCode;
    private String tk;  // Token
    private String mp;  // Market Protection
    private String pc;  // Product Code
    private String dd;  // Disclosed Quantity
    private String dq;  // Disclosed Quantity
    private String vd;  // Validity
    private String ts;  // Trading Symbol
    private String tt;  // Transaction Type
    private String pr;  // Price
    private String tp;  // Trigger Price
    private String qt;  // Quantity
    private String no;  // Order Number
    private String es;  // Exchange Segment
    private String pt;  // Price Type

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

    public String getTk() {
        return tk;
    }

    public void setTk(String tk) {
        this.tk = tk;
    }

    public String getMp() {
        return mp;
    }

    public void setMp(String mp) {
        this.mp = mp;
    }

    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }

    public String getDd() {
        return dd;
    }

    public void setDd(String dd) {
        this.dd = dd;
    }

    public String getDq() {
        return dq;
    }

    public void setDq(String dq) {
        this.dq = dq;
    }

    public String getVd() {
        return vd;
    }

    public void setVd(String vd) {
        this.vd = vd;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getTt() {
        return tt;
    }

    public void setTt(String tt) {
        this.tt = tt;
    }

    public String getPr() {
        return pr;
    }

    public void setPr(String pr) {
        this.pr = pr;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getQt() {
        return qt;
    }

    public void setQt(String qt) {
        this.qt = qt;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getEs() {
        return es;
    }

    public void setEs(String es) {
        this.es = es;
    }

    public String getPt() {
        return pt;
    }

    public void setPt(String pt) {
        this.pt = pt;
    }
}