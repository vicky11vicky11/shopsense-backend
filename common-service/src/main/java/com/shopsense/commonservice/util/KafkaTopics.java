package com.shopsense.commonservice.util;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_EVENTS = "order-events";
    public static final String ORDER_SAGA_RESULTS = "order-saga-results";
    public static final String INVENTORY_COMMANDS = "inventory-commands";
    public static final String INVENTORY_EVENTS = "inventory-events";
    public static final String PAYMENT_COMMANDS = "payment-commands";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String SAGA_EVENTS = "saga-events";
}
