package com.shopsense.commonservice.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryReservationRequestedEvent extends KafkaBaseEvent {

    private UUID orderId;

    private List<ReservationItem> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationItem {

        private UUID productId;

        private Integer quantity;
    }

    public static InventoryReservationRequestedEvent of( UUID orderId, List<ReservationItem> items, UUID correlationId, String idempotencyKey ) {
        return InventoryReservationRequestedEvent.builder()
                .eventType(EventType.INVENTORY_RESERVATION_REQUESTED)
                .eventSource(EventSource.ORCHESTRATOR_SERVICE)
                .correlationId(correlationId)
                .idempotencyKey(idempotencyKey)
                .orderId(orderId)
                .items(items)
                .build();
    }

}
