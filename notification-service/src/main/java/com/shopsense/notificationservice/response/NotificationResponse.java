package com.shopsense.notificationservice.response;

import com.shopsense.commonservice.enums.NotificationEventType;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;

    private UUID eventId;

    private NotificationEventType eventType;

    private NotificationChannel notificationChannel;

    private String recipient;

    private String subject;

    private String content;

    private NotificationStatus status;

    private String failureReason;

    private Instant sentAt;

    private Instant createdAt;

    private Instant updatedAt;
}
