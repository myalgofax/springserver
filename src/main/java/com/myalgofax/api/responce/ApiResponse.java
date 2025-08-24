package com.myalgofax.api.responce;

public class ApiResponse<T> {
    private String status;
    private String message;
    private String userToken;
    private boolean otpRequired;
    
    private T data;

    public ApiResponse(String status, String message, T data, String userToken, boolean otpRequired) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.userToken = userToken;
        this.otpRequired = otpRequired;
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

	@Override
	public String toString() {
		return "ApiResponse [status=" + status + ", message=" + message + ", userToken=" + userToken + ", otpRequired="
				+ otpRequired + ", data=" + data + "]";
	}
}

