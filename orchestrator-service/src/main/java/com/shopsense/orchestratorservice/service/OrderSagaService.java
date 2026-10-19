package com.shopsense.orchestratorservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.enums.OrderStatus;
import com.shopsense.commonservice.event.*;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.orchestratorservice.entity.*;
import com.shopsense.orchestratorservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final ObjectMapper mapper;

    private final SagaStateRepository sagaStateRepository;

    private final SagaEventRecordRepository sagaEventRecordRepository;

    private final SagaCommandRecordRepository sagaCommandRecordRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void handle( String payload ) throws Exception {
        JsonNode node = mapper.readTree(payload);
        EventType eventType = EventType.valueOf(node.path("eventType")
                .asText());
        UUID eventId = UUID.fromString(node.path("eventId")
                .asText());
        if ( sagaEventRecordRepository.existsByEventId(eventId) ) return;
        UUID orderId = UUID.fromString(node.path("orderId")
                .asText());
        UUID correlationId = UUID.fromString(node.path("correlationId")
                .asText());
        sagaEventRecordRepository.save(SagaEventRecord.builder()
                .eventId(eventId)
                .orderId(orderId)
                .correlationId(correlationId)
                .eventType(eventType.name())
                .payload(payload)
                .build());
        switch ( eventType ) {
            case ORDER_CREATED -> created(mapper.readValue(payload, OrderCreatedEvent.class));
            case INVENTORY_RESERVATION_COMPLETED -> reservationCompleted(orderId, correlationId);
            case INVENTORY_RESERVATION_FAILED -> reservationFailed(orderId, correlationId, node.path("reason")
                    .asText("Inventory reservation failed"));
            case PAYMENT_COMPLETED -> paymentCompleted(orderId, correlationId);
            case PAYMENT_FAILED -> paymentFailed(orderId, correlationId, node.path("reason")
                    .asText("Payment failed"));
            case INVENTORY_CONSUMED -> consumed(orderId, correlationId);
            case INVENTORY_RELEASED -> released(orderId, correlationId);
            default -> {
            }
        }
    }

    private void created( OrderCreatedEvent event ) throws Exception {
        if ( sagaStateRepository.findByOrderId(event.getOrderId())
                .isPresent() ) return;
        SagaState state = sagaStateRepository.save(SagaState.builder()
                .orderId(event.getOrderId())
                .correlationId(event.getCorrelationId())
                .userId(event.getUserId())
                .amount(event.getTotalAmount())
                .currency(event.getCurrency()
                        .name())
                .status("WAITING_FOR_RESERVATION")
                .build());
        List<InventoryReservationRequestedEvent.ReservationItem> items = event.getItems()
                .stream()
                .map(i -> InventoryReservationRequestedEvent.ReservationItem.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .build())
                .toList();
        command(state, KafkaTopics.INVENTORY_COMMANDS, InventoryReservationRequestedEvent.of(event.getOrderId(), items, event.getCorrelationId(), commandKey(event.getOrderId(), EventType.INVENTORY_RESERVATION_REQUESTED)));
    }

    private void reservationCompleted( UUID orderId, UUID correlationId ) throws Exception {
        SagaState s = get(orderId);
        if ( !s.getStatus()
                .equals("WAITING_FOR_RESERVATION") ) return;
        s.setStatus("WAITING_FOR_PAYMENT");
        command(s, KafkaTopics.ORDER_SAGA_RESULTS, OrderStatusUpdateRequestedEvent.of(orderId, OrderStatus.PAYMENT_PENDING, "Inventory reserved; awaiting payment", correlationId, commandKey(orderId, EventType.ORDER_STATUS_UPDATE_REQUESTED)));
    }

    private void reservationFailed( UUID orderId, UUID correlationId, String reason ) throws Exception {
        SagaState s = get(orderId);
        if ( terminal(s) ) return;
        s.setStatus("CANCELLED");
        s.setFailureReason(reason);
        command(s, KafkaTopics.ORDER_SAGA_RESULTS, OrderStatusUpdateRequestedEvent.of(orderId, OrderStatus.CANCELLED, reason, correlationId, commandKey(orderId, EventType.ORDER_CANCELLED)));
    }

    private void paymentCompleted( UUID orderId, UUID correlationId ) throws Exception {
        SagaState s = get(orderId);
        if ( !s.getStatus()
                .equals("WAITING_FOR_PAYMENT") ) return;
        s.setStatus("WAITING_FOR_CONSUMPTION");
        command(s, KafkaTopics.INVENTORY_COMMANDS, InventoryOperationRequestedEvent.of(orderId, correlationId, commandKey(orderId, EventType.INVENTORY_CONSUMPTION_REQUESTED), EventType.INVENTORY_CONSUMPTION_REQUESTED));
    }

    private void paymentFailed( UUID orderId, UUID correlationId, String reason ) throws Exception {
        SagaState s = get(orderId);
        if ( terminal(s) ) return;
        s.setStatus("WAITING_FOR_RELEASE");
        s.setFailureReason(reason);
        command(s, KafkaTopics.INVENTORY_COMMANDS, InventoryOperationRequestedEvent.of(orderId, correlationId, commandKey(orderId, EventType.INVENTORY_RELEASE_REQUESTED), EventType.INVENTORY_RELEASE_REQUESTED));
    }

    private void consumed( UUID orderId, UUID correlationId ) throws Exception {
        SagaState s = get(orderId);
        if ( !s.getStatus()
                .equals("WAITING_FOR_CONSUMPTION") ) return;
        s.setStatus("COMPLETED");
        command(s, KafkaTopics.ORDER_SAGA_RESULTS, OrderStatusUpdateRequestedEvent.of(orderId, OrderStatus.CONFIRMED, "Payment and inventory confirmed", correlationId, commandKey(orderId, EventType.ORDER_CONFIRMED)));
    }

    private void released( UUID orderId, UUID correlationId ) throws Exception {
        SagaState s = get(orderId);
        if ( !s.getStatus()
                .equals("WAITING_FOR_RELEASE") ) return;
        s.setStatus("CANCELLED");
        command(s, KafkaTopics.ORDER_SAGA_RESULTS, OrderStatusUpdateRequestedEvent.of(orderId, OrderStatus.CANCELLED, s.getFailureReason(), correlationId, commandKey(orderId, EventType.ORDER_CANCELLED)));
    }

    private SagaState get( UUID orderId ) {
        return sagaStateRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Saga not found for order " + orderId));
    }

    private boolean terminal( SagaState s ) {
        return s.getStatus()
                .equals("COMPLETED") || s.getStatus()
                .equals("CANCELLED");
    }

    private String commandKey( UUID orderId, EventType type ) {
        return orderId + ":" + type;
    }

    private void command( SagaState sagaState, String topic, KafkaBaseEvent event ) throws Exception {
        SagaCommandRecord queuedCommand = sagaCommandRecordRepository.save(SagaCommandRecord.builder()
                .correlationId(sagaState.getCorrelationId())
                .orderId(sagaState.getOrderId())
                .topic(topic)
                .commandType(event.getEventType()
                        .name())
                .payload(mapper.writeValueAsString(event))
                .build());
        applicationEventPublisher.publishEvent(queuedCommand);
    }
}
