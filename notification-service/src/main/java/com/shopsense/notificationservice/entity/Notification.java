package com.shopsense.notificationservice.entity;

import com.shopsense.commonservice.enums.NotificationEventType;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", uniqueConstraints = { @UniqueConstraint(name = "uk_notification_event_channel", columnNames = { "event_id", "notification_channel" }) }, indexes = { @Index(name = "idx_notification_channel", columnList = "notification_channel"), @Index(name = "idx_notification_event_id", columnList = "event_id") })
public class Notification {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel notificationChannel;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private Instant sentAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}