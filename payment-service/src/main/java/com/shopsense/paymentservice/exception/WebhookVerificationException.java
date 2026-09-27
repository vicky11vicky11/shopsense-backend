package com.shopsense.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class WebhookVerificationException extends PaymentException {

    public WebhookVerificationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
