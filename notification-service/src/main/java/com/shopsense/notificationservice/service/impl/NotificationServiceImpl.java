package com.shopsense.notificationservice.service.impl;

import com.shopsense.commonservice.enums.NotificationEventType;
import com.shopsense.commonservice.response.PageResponse;
import com.shopsense.notificationservice.entity.Notification;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationStatus;
import com.shopsense.notificationservice.repository.NotificationRepository;
import com.shopsense.notificationservice.response.NotificationResponse;
import com.shopsense.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public PageResponse<NotificationResponse> getAllNotifications( NotificationChannel notificationChannel, Pageable pageable ) {
        Page<Notification> notifications = notificationRepository.findAll(buildSpecification(notificationChannel, null, null, null), pageable);
        log.info("Fetched all notifications with size {}, page {} and sort {}.", pageable.getPageSize(), pageable.getPageNumber(), pageable.getSort());
        return PageResponse.from(notifications, this::toResponse);
    }

    @Override
    public PageResponse<NotificationResponse> getAllNotificationsByStatus( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus ) {
        Page<Notification> notifications = notificationRepository.findAll(buildSpecification(notificationChannel, notificationStatus, null, null), pageable);
        log.info("Fetched all notifications  status {}, size {}, page {} and sort {}.", notificationStatus, pageable.getPageSize(), pageable.getPageNumber(), pageable.getSort());
        return PageResponse.from(notifications, this::toResponse);
    }

    @Override
    public PageResponse<NotificationResponse> getAllNotificationsByEventType( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus, NotificationEventType eventType ) {
        Page<Notification> notifications = notificationRepository.findAll(buildSpecification(notificationChannel, notificationStatus, eventType, null), pageable);
        log.info("Fetched all notifications  eventType {}, size {}, page {} and sort {}.", eventType, pageable.getPageSize(), pageable.getPageNumber(), pageable.getSort());
        return PageResponse.from(notifications, this::toResponse);
    }

    @Override
    public PageResponse<NotificationResponse> getAllNotificationsByRecipient( Pageable pageable, NotificationChannel notificationChannel, NotificationStatus notificationStatus, NotificationEventType eventType, String recipientEmail ) {
        Page<Notification> notifications = notificationRepository.findAll(buildSpecification(notificationChannel, notificationStatus, eventType, recipientEmail), pageable);
        log.info("Fetched notifications for recipient {}, channel {}, status {}, event type {}, size {}, page {} and sort {}.", recipientEmail, notificationChannel, notificationStatus, eventType, pageable.getPageSize(), pageable.getPageNumber(), notifications.getSort());
        return PageResponse.from(notifications, this::toResponse);
    }

    private Specification<Notification> buildSpecification( NotificationChannel notificationChannel, NotificationStatus notificationStatus, NotificationEventType eventType, String recipientEmail ) {
        Specification<Notification> notificationSpecification = ( ( root, query, criteriaBuilder ) -> criteriaBuilder.conjunction() );
        if ( notificationChannel != null ) {
            notificationSpecification = notificationSpecification.and(( ( root, query, criteriaBuilder ) -> criteriaBuilder.equal(root.get("notificationChannel"), notificationChannel) ));
        }
        if ( notificationStatus != null ) {
            notificationSpecification = notificationSpecification.and(( ( root, query, criteriaBuilder ) -> criteriaBuilder.equal(root.get("status"), notificationStatus) ));
        }
        if ( eventType != null ) {
            notificationSpecification = notificationSpecification.and(( ( root, query, criteriaBuilder ) -> criteriaBuilder.equal(root.get("eventType"), eventType) ));
        }
        if ( recipientEmail != null && !recipientEmail.isEmpty() ) {
            notificationSpecification = notificationSpecification.and(( root, query, criteriaBuilder ) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("recipient")), recipientEmail.trim()
                    .toLowerCase(java.util.Locale.ROOT)));
        }
        return notificationSpecification;
    }

    private NotificationResponse toResponse( Notification notification ) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .eventType(notification.getEventType())
                .notificationChannel(notification.getNotificationChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .failureReason(notification.getFailureReason())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
