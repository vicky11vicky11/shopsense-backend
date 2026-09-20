package com.shopsense.gatewayservice.exceptions;

public class GatewayServiceUnavailableException extends RuntimeException {

    public GatewayServiceUnavailableException( String message ) {
        super(message);
    }
}