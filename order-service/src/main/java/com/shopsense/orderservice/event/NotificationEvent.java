package com.shopsense.orderservice.event;

import com.shopsense.orderservice.enums.NotificationEventType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID eventId;
    private NotificationEventType eventType;
    private String recipient;
    private String content;
}