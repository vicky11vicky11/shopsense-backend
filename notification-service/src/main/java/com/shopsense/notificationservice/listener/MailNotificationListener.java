package com.shopsense.notificationservice.listener;

import com.shopsense.commonservice.event.NotificationEvent;
import com.shopsense.commonservice.util.RabbitMQConstants;
import com.shopsense.notificationservice.request.MailRequest;
import com.shopsense.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailNotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConstants.MAIL_QUEUE)
    public void handleNotification( NotificationEvent event ) {
        log.info("Received mail notification, eventId={}, eventType={}, recipient={}", event.getEventId(), event.getEventType(), event.getRecipient());
        MailRequest request = MailRequest.builder()
                .recipient(event.getRecipient())
                .content(event.getContent())
                .build();
        switch ( event.getEventType() ) {
            case USER_REGISTERED -> emailService.sendUserRegisteredEmail(event.getEventId(), request);
            case USER_UPDATED -> emailService.sendUserUpdatedEmail(event.getEventId(),request);
            case USER_DELETED -> emailService.sendUserDeletedEmail(event.getEventId(),request);
            case ORDER_PLACED -> emailService.sendOrderPlacedEmail(event.getEventId(), request);
            case ORDER_CANCELLED -> emailService.sendOrderCancelledEmail(event.getEventId(), request);
            case PAYMENT_PAID -> emailService.sendPaymentPaidEmail(event.getEventId(), request);
            case PAYMENT_FAILED -> emailService.sendPaymentFailedEmail(event.getEventId(), request);
            case REFUND_SUCCESS -> emailService.sendRefundSuccessEmail(event.getEventId(), request);
        }
        log.info("Mail notification processed, eventId={}", event.getEventId());
    }
}