package com.shopsense.commonservice.event;

import com.shopsense.commonservice.enums.Currency;
import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent extends KafkaBaseEvent {

    private UUID orderId;

    private UUID userId;

    private BigDecimal totalAmount;

    private Currency currency;

    private List<OrderItem> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        private UUID productId;

        private Integer quantity;
    }

    public static OrderCreatedEvent of( UUID orderId, UUID userId, BigDecimal totalAmount, Currency currency,
                                        List<OrderItem> items, UUID correlationId, String idempotencyKey ) {
        return OrderCreatedEvent.builder()
                .eventType(EventType.ORDER_CREATED)
                .eventSource(EventSource.ORDER_SERVICE)
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .currency(currency)
                .items(items)
                .correlationId(correlationId)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
