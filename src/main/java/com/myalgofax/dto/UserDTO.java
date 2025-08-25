package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
	
	
    private String firstName;

    
    private String lastName;
    public String userId;
    public String email;

    private boolean active = true;
    
    public String password;
    
    public String mpin;
    
    @JsonProperty("newMpin")
    public String newMpin;
    
    @JsonProperty("confirmMpin")
    public String confirmMpin;
    
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getMpin() {
		return mpin;
	}
	public void setMpin(String mpin) {
		this.mpin = mpin;
	}
	public String getNewMpin() {
		return newMpin;
	}
	public void setNewMpin(String newMpin) {
		this.newMpin = newMpin;
	}
	public String getConfirmMpin() {
		return confirmMpin;
	}
	public void setConfirmMpin(String confirmMpin) {
		this.confirmMpin = confirmMpin;
	}
	@Override
	public String toString() {
		return "UserDTO [firstName=" + firstName + ", lastName=" + lastName + ", userId=" + userId + ", email=" + email
				+ ", active=" + active + ", password=" + password + ", mpin=" + mpin + ", newMpin=" + newMpin
				+ ", confirmMpin=" + confirmMpin + "]";
	}
    
    
}
