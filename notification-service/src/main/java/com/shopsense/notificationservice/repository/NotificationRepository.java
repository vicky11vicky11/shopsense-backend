package com.shopsense.notificationservice.repository;

import com.shopsense.notificationservice.entity.Notification;
import com.shopsense.notificationservice.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByEventIdAndNotificationChannel( UUID eventId, NotificationChannel channel );

}