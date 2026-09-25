package com.shopsense.gatewayservice.exception;

public class GatewayServiceUnavailableException extends RuntimeException {

    public GatewayServiceUnavailableException( String message ) {
        super(message);
    }
}