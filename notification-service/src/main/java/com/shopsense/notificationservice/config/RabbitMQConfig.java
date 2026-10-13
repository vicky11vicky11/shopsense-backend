package com.shopsense.notificationservice.config;

import com.shopsense.commonservice.util.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(RabbitMQConstants.NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue mailQueue() {
        return new Queue(RabbitMQConstants.MAIL_QUEUE, true);
    }

    @Bean
    public Binding mailBinding( Queue mailQueue, TopicExchange notificationExchange ) {
        return BindingBuilder.bind(mailQueue)
                .to(notificationExchange)
                .with(RabbitMQConstants.MAIL_ROUTING_KEY);
    }
}