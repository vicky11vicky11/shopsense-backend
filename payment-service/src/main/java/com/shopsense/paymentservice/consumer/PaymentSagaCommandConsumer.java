package com.shopsense.paymentservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.event.PaymentRequestedEvent;
import com.shopsense.commonservice.util.KafkaGroups;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.paymentservice.service.SagaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSagaCommandConsumer {

    private final ObjectMapper mapper;

    private final SagaPaymentService payments;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMMANDS, groupId = KafkaGroups.PAYMENT_GROUP)
    public void consume( String payload ) throws Exception {
        if ( mapper.readTree(payload)
                .path("eventType")
                .asText()
                .equals(EventType.PAYMENT_REQUESTED.name()) ) payments.request(mapper.readValue(payload, PaymentRequestedEvent.class));
    }
}
