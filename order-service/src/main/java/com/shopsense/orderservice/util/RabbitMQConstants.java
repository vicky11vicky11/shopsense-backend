package com.shopsense.orderservice.util;

public final class RabbitMQConstants {
    private RabbitMQConstants() {
    }

    public static final String NOTIFICATION_EXCHANGE = "shopsense.notification.exchange";
    public static final String MAIL_ROUTING_KEY = "notification.mail";
}