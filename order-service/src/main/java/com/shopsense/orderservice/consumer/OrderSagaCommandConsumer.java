package com.shopsense.orderservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.event.OrderStatusUpdateRequestedEvent;
import com.shopsense.commonservice.util.KafkaGroups;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSagaCommandConsumer {

    private final ObjectMapper objectMapper;

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.ORDER_SAGA_RESULTS, groupId = KafkaGroups.ORDER_GROUP)
    public void consume( String payload ) throws Exception {
        if ( objectMapper.readTree(payload)
                .path("eventType")
                .asText()
                .equals(EventType.ORDER_STATUS_UPDATE_REQUESTED.name()) ) {
            OrderStatusUpdateRequestedEvent event = objectMapper.readValue(payload, OrderStatusUpdateRequestedEvent.class);
            orderService.applySagaStatus(event);
        }
    }
}
