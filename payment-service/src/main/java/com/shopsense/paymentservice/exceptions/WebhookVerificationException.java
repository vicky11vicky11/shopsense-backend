package com.shopsense.paymentservice.exceptions;

import org.springframework.http.HttpStatus;

public class WebhookVerificationException extends PaymentException {

    public WebhookVerificationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
