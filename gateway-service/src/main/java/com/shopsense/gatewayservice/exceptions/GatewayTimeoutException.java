package com.shopsense.gatewayservice.exceptions;

public class GatewayTimeoutException extends RuntimeException {
    public GatewayTimeoutException( String message ) {
        super(message);
    }
}