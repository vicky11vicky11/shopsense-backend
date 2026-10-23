package com.shopsense.notificationservice.controller;

import com.shopsense.commonservice.enums.NotificationEventType;
import com.shopsense.commonservice.response.PageResponse;
import com.shopsense.commonservice.util.AppUrl;
import com.shopsense.notificationservice.enums.NotificationChannel;
import com.shopsense.notificationservice.enums.NotificationStatus;
import com.shopsense.notificationservice.response.NotificationResponse;
import com.shopsense.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(AppUrl.NOTIFICATION_URL)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<NotificationResponse>> getAllNotifications( @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) NotificationChannel notificationChannel ) {
        PageResponse<NotificationResponse> notifications = notificationService.getAllNotifications(notificationChannel, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/status")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotificationsByStatus( @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) NotificationChannel notificationChannel, @RequestParam NotificationStatus notificationStatus ) {
        PageResponse<NotificationResponse> notifications = notificationService.getAllNotificationsByStatus(pageable, notificationChannel, notificationStatus);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/event-type")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotificationsByEventType( @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) NotificationChannel notificationChannel, @RequestParam(required = false) NotificationStatus notificationStatus, @RequestParam NotificationEventType eventType ) {
        PageResponse<NotificationResponse> notifications = notificationService.getAllNotificationsByEventType(pageable, notificationChannel, notificationStatus, eventType);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/recipient")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotificationsByRecipient( @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) NotificationChannel notificationChannel, @RequestParam(required = false) NotificationStatus notificationStatus, @RequestParam(required = false) NotificationEventType eventType, @RequestParam String recipientEmail ) {
        PageResponse<NotificationResponse> notifications = notificationService.getAllNotificationsByRecipient(pageable, notificationChannel, notificationStatus, eventType, recipientEmail);
        return ResponseEntity.ok(notifications);
    }

}
