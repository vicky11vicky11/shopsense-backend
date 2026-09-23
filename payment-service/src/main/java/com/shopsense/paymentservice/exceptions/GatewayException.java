package com.shopsense.paymentservice.exceptions;

import org.springframework.http.HttpStatus;

public class GatewayException extends PaymentException {

    public GatewayException( String message ) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public GatewayException( String message, Throwable cause ) {
        super(message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
