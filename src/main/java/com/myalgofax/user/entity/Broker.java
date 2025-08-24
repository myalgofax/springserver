package com.myalgofax.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myalgofax.security.util.Encryptor;

@Table("brokers")
public class Broker {

	@Id
	private Long brokerId;

	@Column("phone_number")
	private String phoneNumber;

	@Column("ucc")
	private String ucc;

	@Column("user_id")
	private String userId;

	@Column("username")
	private String username;

	@Column("broker_code")
	private String brokerCode;
	
	@Column("password")
	private String password;

	@JsonIgnore
	@Column("consumerkey")
	private String consumerkey;

	@Column("consumer_secret_key")
	private String consumerSecretKey;

	@Column("active_inv")
	private String activeInv;

	@JsonIgnore
	@Column("totp")
	private String totp;
	
	@Column("neo_fin_key")
	private String neoFinKey;

	public Long getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(Long id) {
		this.brokerId = id;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUcc() {
		return ucc;
	}

	public void setUcc(String ucc) {
		this.ucc = ucc;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBrokerCode() {
		return brokerCode;
	}

	public void setBrokerCode(String brokerCode) {
		this.brokerCode = brokerCode;
	}

	public String getPassword() {
		return password != null ? getEncryptor().decode(password) : null;
	}

	public void setPassword(String password) {
		this.password = password != null ? getEncryptor().encode(password) : null;
	}

	public String getConsumerkey() {
		return consumerkey != null ? getEncryptor().decode(consumerkey) : null;
	}

	public void setConsumerkey(String consumerkey) {
		this.consumerkey = consumerkey != null ? getEncryptor().encode(consumerkey) : null;
	}

	public String getConsumerSecretKey() {
		return consumerSecretKey != null ? getEncryptor().decode(consumerSecretKey) : null;
	}

	public void setConsumerSecretKey(String consumerSecretKey) {
		this.consumerSecretKey = consumerSecretKey != null ? getEncryptor().encode(consumerSecretKey) : null;
	}

	public String getActiveInv() {
		return activeInv;
	}

	public Broker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Broker(Long id, String phoneNumber, String ucc, String userId, String username, String brokerCode,
			String password, String consumerkey, String consumerSecretKey, String activeInv, String totp) {
		super();
		this.brokerId = id;
		this.phoneNumber = phoneNumber;
		this.ucc = ucc;
		this.userId = userId;
		this.username = username;
		this.brokerCode = brokerCode;
		this.password = password;
		this.consumerkey = consumerkey;
		this.consumerSecretKey = consumerSecretKey;
		this.activeInv = activeInv;
		this.totp = totp;
	}

	@Override
	public String toString() {
		return "Broker [id=" + brokerId + ", phoneNumber=" + phoneNumber + ", ucc=" + ucc + ", userId=" + userId
				+ ", username=" + username + ", brokerCode=" + brokerCode + ", password=" + password + ", consumerkey="
				+ consumerkey + ", consumerSecretKey=" + consumerSecretKey + ", activeInv=" + activeInv + ", totp="
				+ totp + ", neoFinKey=" + neoFinKey + "]";
	}

	public void setActiveInv(String activeInv) {
		this.activeInv = activeInv;
	}

	public String getTotp() {
		return totp != null ? getEncryptor().decode(totp) : null;
	}

	public void setTotp(String totp) {
		this.totp = totp != null ? getEncryptor().encode(totp) : null;
	}

	private static Encryptor encryptor;

	public static void setEncryptor(Encryptor encryptor) {
		Broker.encryptor = encryptor;
	}

	private Encryptor getEncryptor() {
		return encryptor;
	}

	public String getNeoFinKey() {
		return neoFinKey;
	}

	public void setNeoFinKey(String neoFinKey) {
		this.neoFinKey = neoFinKey;
	}


}