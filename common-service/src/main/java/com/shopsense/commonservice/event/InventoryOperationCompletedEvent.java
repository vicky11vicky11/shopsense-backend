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
public class InventoryOperationCompletedEvent extends KafkaBaseEvent {

    private UUID orderId;

    public static InventoryOperationCompletedEvent of( UUID orderId, UUID correlationId, String key, EventType type ) {
        return InventoryOperationCompletedEvent.builder()
                .eventType(type)
                .eventSource(EventSource.INVENTORY_SERVICE)
                .orderId(orderId)
                .correlationId(correlationId)
                .idempotencyKey(key)
                .build();
    }
}
