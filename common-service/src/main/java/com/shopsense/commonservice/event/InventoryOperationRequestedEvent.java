package com.shopsense.commonservice.event;

import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOperationRequestedEvent extends KafkaBaseEvent {

    private UUID orderId;

    public static InventoryOperationRequestedEvent of( UUID orderId, UUID correlationId, String key, EventType type ) {
        return InventoryOperationRequestedEvent.builder()
                .eventType(type)
                .eventSource(EventSource.ORCHESTRATOR_SERVICE)
                .orderId(orderId)
                .correlationId(correlationId)
                .idempotencyKey(key)
                .build();
    }
}
