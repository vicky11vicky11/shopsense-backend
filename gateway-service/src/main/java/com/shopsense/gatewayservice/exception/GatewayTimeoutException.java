package com.shopsense.gatewayservice.exception;

public class GatewayTimeoutException extends RuntimeException {
    public GatewayTimeoutException( String message ) {
        super(message);
    }
}