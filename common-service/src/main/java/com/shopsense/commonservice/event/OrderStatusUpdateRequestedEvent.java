package com.shopsense.commonservice.event;

import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.enums.OrderStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequestedEvent extends KafkaBaseEvent {

    private UUID orderId;

    private OrderStatus status;

    private String reason;

    public static OrderStatusUpdateRequestedEvent of( UUID orderId, OrderStatus status, String reason, UUID correlationId, String key ) {
        return OrderStatusUpdateRequestedEvent.builder()
                .eventType(EventType.ORDER_STATUS_UPDATE_REQUESTED)
                .eventSource(EventSource.ORCHESTRATOR_SERVICE)
                .orderId(orderId)
                .status(status)
                .reason(reason)
                .correlationId(correlationId)
                .idempotencyKey(key)
                .build();
    }
}
