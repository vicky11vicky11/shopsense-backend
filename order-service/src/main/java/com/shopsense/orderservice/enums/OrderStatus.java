package com.shopsense.orderservice.enums;

public enum OrderStatus {

    PENDING_RESERVATION,
    RESERVED,
    PAYMENT_PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    PAYMENT_FAILED,
    CANCELLED;

}