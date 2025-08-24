package com.myalgofax.brockerAPI.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:zerodha.properties")
@ConfigurationProperties(prefix = "broker.api")
public class ZerodhaApiConfig {
	
	 private String baseUrl;
	    private String baseUrl2;
	    private String oauthToken;
	    private String instruments;
	    private String quotes;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl2() {
		return baseUrl2;
	}

	public void setBaseUrl2(String baseUrl2) {
		this.baseUrl2 = baseUrl2;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getInstruments() {
		return instruments;
	}

	public void setInstruments(String instruments) {
		this.instruments = instruments;
	}

	public String getQuotes() {
		return quotes;
	}

	public void setQuotes(String quotes) {
		this.quotes = quotes;
	}
}
