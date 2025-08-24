package com.myalgofax.dto;

public class WebSocketAuthDto {
    private String userToken;
    private String message;

    public WebSocketAuthDto() {}

    public WebSocketAuthDto(String userToken, String message) {
        this.userToken = userToken;
        this.message = message;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}