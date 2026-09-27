package com.shopsense.notificationservice.service.impl;

import com.shopsense.notificationservice.entity.Notification;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationEventType;
import com.shopsense.notificationservice.enums.NotificationStatus;
import com.shopsense.notificationservice.repository.NotificationRepository;
import com.shopsense.notificationservice.request.MailRequest;
import com.shopsense.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.from}")
    private String from;

    private final JavaMailSender mailSender;

    private final NotificationRepository notificationRepository;

    @Override
    public void sendUserRegisteredEmail( UUID eventId, MailRequest request ) {
        String title = "Welcome to ShopSense";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Thank you for joining ShopSense.</p>";
        sendEmail(eventId, NotificationEventType.USER_REGISTERED, request, "ShopSense : Welcome to ShopSense", buildHtmlEmail(title, content));
    }

    @Override
    public void sendUserUpdatedEmail( UUID eventId, MailRequest request ) {
        String title = "User Details Updated Successfully";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Thank you for using ShopSense.</p>";
        sendEmail(eventId, NotificationEventType.USER_UPDATED, request, "ShopSense : User Details Updated", buildHtmlEmail(title, content));
    }

    @Override
    public void sendUserDeletedEmail( UUID eventId, MailRequest request ) {
        String title = "User Account Deleted Successfully";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Thank you for using ShopSense.</p>";
        sendEmail(eventId, NotificationEventType.USER_DELETED, request, "ShopSense : User Account Deleted", buildHtmlEmail(title, content));
    }

    @Override
    public void sendOrderPlacedEmail( UUID eventId, MailRequest request ) {
        String title = "Order Placed Successfully";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Thank you for shopping with ShopSense.</p>";
        sendEmail(eventId, NotificationEventType.ORDER_PLACED, request, "ShopSense : Order Placed Successfully", buildHtmlEmail(title, content));
    }

    @Override
    public void sendPaymentPaidEmail( UUID eventId, MailRequest request ) {
        String title = "Payment Successful";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Thank you for your payment.</p>";
        sendEmail(eventId, NotificationEventType.PAYMENT_PAID, request, "ShopSense : Payment Successful", buildHtmlEmail(title, content));
    }

    @Override
    public void sendPaymentFailedEmail( UUID eventId, MailRequest request ) {
        String title = "Payment Failed";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Kindly retry your payment.</p>";
        sendEmail(eventId, NotificationEventType.PAYMENT_FAILED, request, "ShopSense : Payment Failed", buildHtmlEmail(title, content));
    }

    @Override
    public void sendOrderCancelledEmail( UUID eventId, MailRequest request ) {
        String title = "Order Cancelled";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>If you have any questions, please contact ShopSense support.</p>";
        sendEmail(eventId, NotificationEventType.ORDER_CANCELLED, request, "ShopSense : Order Cancelled", buildHtmlEmail(title, content));
    }

    @Override
    public void sendRefundSuccessEmail( UUID eventId, MailRequest request ) {
        String title = "Refund Success";
        String content = "<p>" + escapeHtml(request.getContent()) + "</p>" + "<p>Your refund amount successfully processed. Kindly check you balance.</p>";
        sendEmail(eventId, NotificationEventType.REFUND_SUCCESS, request, "ShopSense : Refund Success", buildHtmlEmail(title, content));
    }

    private String buildHtmlEmail( String title, String contentHtml ) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%1$s</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f5f7; font-family: Arial, Helvetica, sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:24px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.08); max-width:600px; width:100%%;">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background-color:#1a73e8; padding:24px 32px;">
                                            <h1 style="margin:0; font-size:20px; line-height:1.3; color:#ffffff; font-weight:600;">
                                                %1$s
                                            </h1>
                                        </td>
                                    </tr>
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding:32px; color:#202124; font-size:15px; line-height:1.6;">
                                            %2$s
                                        </td>
                                    </tr>
                                    <!-- Divider -->
                                    <tr>
                                        <td style="padding:0 32px;">
                                            <hr style="border:none; border-top:1px solid #e8eaed; margin:0;">
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding:20px 32px 28px; color:#5f6368; font-size:12px; line-height:1.6;">
                                            Regards,<br>
                                            <strong>ShopSense Team</strong>
                                            <br><br>
                                            This is an automated message, please do not reply directly to this email.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(title, contentHtml);
    }

    private String escapeHtml( String input ) {
        if ( input == null ) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }

    private void sendEmail( UUID eventId, NotificationEventType eventType, MailRequest request, String subject, String htmlBody ) {
        log.info("Processing email notification, eventId={}, eventType={}, recipient={}", eventId, eventType, request.getRecipient());
        Notification existingNotification = notificationRepository.findByEventIdAndNotificationChannel(eventId, NotificationChannel.MAIL)
                .orElse(null);
        if ( existingNotification != null ) {
            if ( existingNotification.getStatus() == NotificationStatus.SENT ) {
                log.info("Email notification already sent, eventId={}, recipient={}", eventId, request.getRecipient());
                return;
            }
            log.info("Retrying existing email notification, eventId={}, status={}", eventId, existingNotification.getStatus());
            existingNotification.setStatus(NotificationStatus.PENDING);
            existingNotification.setFailureReason(null);
            existingNotification.setSentAt(null);
            existingNotification.setRecipient(request.getRecipient());
            existingNotification.setSubject(subject);
            existingNotification.setContent(htmlBody);
            notificationRepository.save(existingNotification);
            sendAndUpdate(existingNotification, htmlBody);
            return;
        }
        Notification notification = Notification.builder()
                .eventId(eventId)
                .eventType(eventType)
                .notificationChannel(NotificationChannel.MAIL)
                .recipient(request.getRecipient())
                .subject(subject)
                .content(htmlBody)
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);
        log.info("Email notification persisted as PENDING, notificationId={}, eventId={}", notification.getId(), eventId);
        sendAndUpdate(notification, htmlBody);
    }

    private void sendAndUpdate( Notification notification, String htmlBody ) {
        try {
            log.info("Sending email, notificationId={}, recipient={}", notification.getId(), notification.getRecipient());
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setFailureReason(null);
            notificationRepository.save(notification);
            log.info("Email sent successfully, notificationId={}, recipient={}", notification.getId(), notification.getRecipient());
        } catch ( MessagingException exception ) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(exception.getMessage());
            notification.setSentAt(null);
            notificationRepository.save(notification);
            log.error("Email message creation failed, notificationId={}, recipient={}, reason={}", notification.getId(), notification.getRecipient(), exception.getMessage(), exception);
            throw new RuntimeException("Failed to create email message", exception);
        } catch ( Exception exception ) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(exception.getMessage());
            notification.setSentAt(null);
            notificationRepository.save(notification);
            log.error("Email sending failed, notificationId={}, recipient={}, reason={}", notification.getId(), notification.getRecipient(), exception.getMessage(), exception);
            throw exception;
        }
    }
}