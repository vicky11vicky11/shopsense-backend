package com.shopsense.notificationservice.service;

import com.shopsense.commonservice.enums.NotificationEventType;
import com.shopsense.commonservice.response.PageResponse;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationStatus;
import com.shopsense.notificationservice.response.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> getAllNotifications( NotificationChannel notificationChannel, Pageable pageable );

    PageResponse<NotificationResponse> getAllNotificationsByStatus( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus );

    PageResponse<NotificationResponse> getAllNotificationsByEventType( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus, NotificationEventType eventType );

    PageResponse<NotificationResponse> getAllNotificationsByRecipient( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus, NotificationEventType eventType, String recipientEmail );
}
