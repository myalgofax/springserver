package com.algo.api.responce;

public class ApiResponse<T> {
    private String status;
    private String message;
    private String userToken;
    
    private T data;

    public ApiResponse(String status, String message, T data, String userToken) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.userToken = userToken;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
    public String getUserToken() {
    	return userToken;
    }

    public T getData() {
        return data;
    }
}

