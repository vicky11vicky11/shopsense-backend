package com.shopsense.notificationservice.service;

import com.shopsense.notificationservice.request.MailRequest;

import java.util.UUID;

public interface EmailService {

    void sendUserRegisteredEmail( UUID eventId, MailRequest request );

    void sendUserUpdatedEmail( UUID eventId, MailRequest request );

    void sendUserDeletedEmail( UUID eventId, MailRequest request );

    void sendOrderPlacedEmail( UUID eventId, MailRequest request );

    void sendPaymentPaidEmail( UUID eventId, MailRequest request );

    void sendPaymentFailedEmail( UUID eventId, MailRequest request );

    void sendOrderCancelledEmail( UUID eventId, MailRequest request );

    void sendRefundSuccessEmail( UUID eventId, MailRequest request );
}