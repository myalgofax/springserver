package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KotakNeoAuthDto {
    
    @JsonProperty("user_id")
    private String userId;
    
    private String ucc;
    
    @JsonProperty("consumer_key")
    private String consumerKey;
    
    @JsonProperty("consumer_secret")
    private String consumerSecret;
    
    private String environment;
    
    @JsonProperty("mobile_number")
    private String mobileNumber;
    
    private String totp;
    
    private String mpin;
    
    private String ott;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUcc() {
        return ucc;
    }

    public void setUcc(String ucc) {
        this.ucc = ucc;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getTotp() {
        return totp;
    }

    public void setTotp(String totp) {
        this.totp = totp;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getOtt() {
        return ott;
    }

    public void setOtt(String ott) {
        this.ott = ott;
    }
}