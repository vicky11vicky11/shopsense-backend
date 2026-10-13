package com.shopsense.userservice.producer;

import com.shopsense.commonservice.event.NotificationEvent;
import com.shopsense.commonservice.util.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishMailNotification( NotificationEvent event ) {
        log.info("Publishing mail notification, eventId={}, eventType={}, recipient={}", event.getEventId(), event.getEventType(), event.getRecipient());
        rabbitTemplate.convertAndSend(RabbitMQConstants.NOTIFICATION_EXCHANGE, RabbitMQConstants.MAIL_ROUTING_KEY, event);
        log.info("Mail notification published, eventId={}", event.getEventId());
    }
}
