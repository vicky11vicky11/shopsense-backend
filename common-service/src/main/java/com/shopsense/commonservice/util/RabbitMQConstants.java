package com.shopsense.commonservice.util;

public final class RabbitMQConstants {
    private RabbitMQConstants() {
    }

    public static final String NOTIFICATION_EXCHANGE = "shopsense.notification.exchange";
    public static final String MAIL_QUEUE = "shopsense.notification.mail.queue";
    public static final String MAIL_ROUTING_KEY = "notification.mail";
    public static final String SMS_QUEUE = "shopsense.notification.sms.queue";
    public static final String SMS_ROUTING_KEY = "notification.sms";
    public static final String PUSH_QUEUE = "shopsense.notification.push.queue";
    public static final String PUSH_ROUTING_KEY = "notification.push";
}