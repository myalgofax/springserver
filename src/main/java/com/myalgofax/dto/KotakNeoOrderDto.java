package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KotakNeoOrderDto {
    
    @JsonProperty("exchange_segment")
    private String exchangeSegment;
    
    private String product;
    
    private String price;
    
    @JsonProperty("order_type")
    private String orderType;
    
    private String quantity;
    
    private String validity;
    
    @JsonProperty("trading_symbol")
    private String tradingSymbol;
    
    @JsonProperty("transaction_type")
    private String transactionType;
    
    private String amo;
    
    @JsonProperty("disclosed_quantity")
    private String disclosedQuantity;
    
    @JsonProperty("market_protection")
    private String marketProtection;
    
    private String pf;
    
    @JsonProperty("trigger_price")
    private String triggerPrice;
    
    private String tag;
    
    @JsonProperty("order_id")
    private String orderId;

    public String getExchangeSegment() {
        return exchangeSegment;
    }

    public void setExchangeSegment(String exchangeSegment) {
        this.exchangeSegment = exchangeSegment;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getAmo() {
        return amo;
    }

    public void setAmo(String amo) {
        this.amo = amo;
    }

    public String getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(String disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public String getMarketProtection() {
        return marketProtection;
    }

    public void setMarketProtection(String marketProtection) {
        this.marketProtection = marketProtection;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(String triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}