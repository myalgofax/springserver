package com.myalgofax.exceptions;

public class BrokerApiException extends RuntimeException {
    public BrokerApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
