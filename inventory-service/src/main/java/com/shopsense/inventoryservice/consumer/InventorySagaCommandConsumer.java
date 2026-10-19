package com.shopsense.inventoryservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.event.*;
import com.shopsense.commonservice.request.ReserveStockRequest;
import com.shopsense.commonservice.util.KafkaGroups;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventorySagaCommandConsumer {

    private final ObjectMapper mapper;

    private final StockReservationService reservations;

    private final KafkaTemplate<String, Object> kafka;

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_COMMANDS, groupId = KafkaGroups.INVENTORY_GROUP)
    public void consume( String payload ) throws Exception {
        JsonNode node = mapper.readTree(payload);
        EventType type = EventType.valueOf(node.path("eventType")
                .asText());
        UUID orderId = UUID.fromString(node.path("orderId")
                .asText());
        UUID correlationId = UUID.fromString(node.path("correlationId")
                .asText());
        String key = node.path("idempotencyKey")
                .asText();
        try {
            KafkaBaseEvent result;
            if ( type == EventType.INVENTORY_RESERVATION_REQUESTED ) {
                InventoryReservationRequestedEvent command = mapper.readValue(payload, InventoryReservationRequestedEvent.class);
                reservations.reserve(ReserveStockRequest.builder()
                        .orderId(orderId)
                        .items(command.getItems()
                                .stream()
                                .map(i -> ReserveStockRequest.ReservationItem.builder()
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .build())
                                .toList())
                        .build());
                result = InventoryReservationCompletedEvent.of(orderId, correlationId, key);
            } else if ( type == EventType.INVENTORY_CONSUMPTION_REQUESTED ) {
                reservations.consumeByOrder(orderId);
                result = InventoryOperationCompletedEvent.of(orderId, correlationId, key, EventType.INVENTORY_CONSUMED);
            } else if ( type == EventType.INVENTORY_RELEASE_REQUESTED ) {
                reservations.releaseByOrder(orderId);
                result = InventoryOperationCompletedEvent.of(orderId, correlationId, key, EventType.INVENTORY_RELEASED);
            } else return;
            kafka.send(KafkaTopics.INVENTORY_EVENTS, orderId.toString(), mapper.writeValueAsString(result));
        } catch ( Exception exception ) {
            if ( type == EventType.INVENTORY_RESERVATION_REQUESTED ) {
                InventoryOperationCompletedEvent failed = InventoryOperationCompletedEvent.of(orderId, correlationId, key, EventType.INVENTORY_RESERVATION_FAILED);
                kafka.send(KafkaTopics.INVENTORY_EVENTS, orderId.toString(), mapper.writeValueAsString(failed));
                return;
            }
            throw exception;
        }
    }
}
