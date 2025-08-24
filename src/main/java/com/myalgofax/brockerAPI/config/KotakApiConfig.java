package com.myalgofax.brockerAPI.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;

@Configuration
@PropertySource("classpath:kotak.properties")
@ConfigurationProperties(prefix = "kotak.api")
public class KotakApiConfig {

	private String key;
	private String secret;
	private String baseUrl;
	private String username;
	private String password;
	private String loginUrl;
	private String generateOtpUrl;
	private String otpAuthUrl;
	private String filePath;

	private String url;
	private Duration pingInterval;
	private Duration timeout;
	private int bufferSize;
	
	public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Duration getPingInterval() { return pingInterval; }
    public void setPingInterval(Duration pingInterval) { this.pingInterval = pingInterval; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	// Getters and Setters
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getGenerateOtpUrl() {
		return generateOtpUrl;
	}

	public void setGenerateOtpUrl(String generateOtpUrl) {
		this.generateOtpUrl = generateOtpUrl;
	}

	public String getOtpAuthUrl() {
		return otpAuthUrl;
	}

	public void setOtpAuthUrl(String otpAuthUrl) {
		this.otpAuthUrl = otpAuthUrl;
	}
}
