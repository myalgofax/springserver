package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BrokerDto {
	
	 	private String username;
	 	
	    private String password;
	    
	    private String brokerCode;
	    
	    private String apiKey;
	    
	    private String apiSecret;
	    
	    private String phoneNumber;
	    
	    private String userId;
	    
	    private String clientId;
	    
	
	    private String totp;
	    

	    private String otp;
	    
	
	    private String accessBrokerToken;
	    
	    @JsonProperty(value = "neoFinKey")
	    @JsonAlias({"neo_fin_key", "neofinkey", "NeoFinKey"})
	    private String neoFinKey;
	    
	
	    
	    

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	
		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getApiSecret() {
			return apiSecret;
		}

		public void setApiSecret(String apiSecret) {
			this.apiSecret = apiSecret;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getTotp() {
			return totp;
		}

		public void setTotp(String totp) {
			this.totp = totp;
		}

		public String getBrokerCode() {
			return brokerCode;
		}

		public void setBrokerCode(String brokerCode) {
			this.brokerCode = brokerCode;
		}

		public String getOtp() {
			return otp;
		}

		public void setOtp(String otp) {
			this.otp = otp;
		}

		public String getAccessBrokerToken() {
			return accessBrokerToken;
		}

		public void setAccessBrokerToken(String accessBrokerToken) {
			this.accessBrokerToken = accessBrokerToken;
		}

	

		@Override
		public String toString() {
			return "BrokerDto [username=" + username + ", password=" + password + ", brokerCode=" + brokerCode
					+ ", apiKey=" + apiKey + ", apiSecret=" + apiSecret + ", phoneNumber=" + phoneNumber + ", userId="
					+ userId + ", clientId=" + clientId + ", totp=" + totp + ", otp=" + otp + ", accessBrokerToken="
					+ accessBrokerToken + ", neoFinKey=" + neoFinKey + "]";
		}

		public String getNeoFinKey() {
			return neoFinKey;
		}

		public void setNeoFinKey(String neoFinKey) {
			this.neoFinKey = neoFinKey;
		}
		
	    
	    
}
