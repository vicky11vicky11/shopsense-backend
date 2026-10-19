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
public class InventoryReservationCompletedEvent extends KafkaBaseEvent {

    private UUID orderId;

    public static InventoryReservationCompletedEvent of( UUID orderId, UUID correlationId, String key ) {
        return InventoryReservationCompletedEvent.builder()
                .eventType(EventType.INVENTORY_RESERVATION_COMPLETED)
                .eventSource(EventSource.INVENTORY_SERVICE)
                .orderId(orderId)
                .correlationId(correlationId)
                .idempotencyKey(key)
                .build();
    }
}
