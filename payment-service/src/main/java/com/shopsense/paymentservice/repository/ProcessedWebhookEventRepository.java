package com.shopsense.paymentservice.repository;

import com.shopsense.paymentservice.entity.ProcessedWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, UUID> {

    boolean existsByGatewayAndEventId( String gateway, String eventId );
}