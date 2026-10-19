package com.shopsense.orchestratorservice.consumer;

import com.shopsense.commonservice.util.KafkaGroups;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.orchestratorservice.service.OrderSagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSagaEventConsumer {

    private final OrderSagaService saga;

    @KafkaListener(topics = { KafkaTopics.ORDER_EVENTS, KafkaTopics.INVENTORY_EVENTS, KafkaTopics.PAYMENT_EVENTS }, groupId = KafkaGroups.ORCHESTRATOR_GROUP)
    public void consume( String payload ) throws Exception {
        saga.handle(payload);
    }
}
