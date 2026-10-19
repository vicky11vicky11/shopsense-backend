package com.shopsense.commonservice.event;

import com.shopsense.commonservice.enums.Currency;
import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent extends KafkaBaseEvent {

    private UUID orderId;

    private UUID userId;

    private BigDecimal amount;

    private Currency currency;

    public static PaymentRequestedEvent of( UUID orderId, UUID userId, BigDecimal amount, Currency currency, UUID correlationId, String key ) {
        return PaymentRequestedEvent.builder()
                .eventType(EventType.PAYMENT_REQUESTED)
                .eventSource(EventSource.ORCHESTRATOR_SERVICE)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .currency(currency)
                .correlationId(correlationId)
                .idempotencyKey(key)
                .build();
    }
}
