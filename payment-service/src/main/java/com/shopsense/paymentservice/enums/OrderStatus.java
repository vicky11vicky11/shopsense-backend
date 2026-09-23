package com.shopsense.paymentservice.enums;

public enum OrderStatus {

    PENDING_RESERVATION,
    RESERVED,
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;
}