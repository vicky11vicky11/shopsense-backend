package com.shopsense.paymentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.commonservice.enums.EventSource;
import com.shopsense.commonservice.enums.EventType;
import com.shopsense.commonservice.event.KafkaBaseEvent;
import com.shopsense.commonservice.event.PaymentRequestedEvent;
import com.shopsense.commonservice.util.KafkaTopics;
import com.shopsense.paymentservice.entity.Payment;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import com.shopsense.paymentservice.provider.PaymentProviderFactory;
import com.shopsense.paymentservice.repository.PaymentRepository;
import com.shopsense.paymentservice.response.GatewayPaymentResponse;
import com.shopsense.paymentservice.request.CreateGatewayPaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaPaymentService {

    private final PaymentRepository payments;

    private final PaymentProviderFactory providers;

    private final KafkaTemplate<String, Object> kafka;

    private final ObjectMapper mapper;

    @Transactional
    public void request( PaymentRequestedEvent event ) throws Exception {
        if ( payments.findFirstByOrderIdOrderByCreatedAtDesc(event.getOrderId())
                .isPresent() ) return;
        try {
            PaymentGateway gateway = PaymentGateway.RAZORPAY;
            GatewayPaymentResponse gatewayPayment = providers.getProvider(gateway)
                    .createPayment(CreateGatewayPaymentRequest.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .amount(event.getAmount())
                            .currency(event.getCurrency())
                            .description("Payment for order " + event.getOrderId())
                            .build());
            payments.save(Payment.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .paymentGateway(gateway)
                    .gatewayOrderId(gatewayPayment.getGatewayOrderId())
                    .gatewayPaymentId(gatewayPayment.getGatewayPaymentId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .correlationId(event.getCorrelationId())
                    .sagaIdempotencyKey(event.getIdempotencyKey())
                    .build());
        } catch ( Exception exception ) {
            publish(event.getOrderId(), event.getCorrelationId(), event.getIdempotencyKey(), EventType.PAYMENT_FAILED, exception.getMessage());
        }
    }

    public void publish( java.util.UUID orderId, java.util.UUID correlationId, String key, EventType type, String reason ) throws Exception {
        KafkaBaseEvent event = new KafkaBaseEvent() {
        };
        event.setEventType(type);
        event.setEventSource(EventSource.PAYMENT_SERVICE);
        event.setCorrelationId(correlationId);
        event.setIdempotencyKey(key);
        kafka.send(KafkaTopics.PAYMENT_EVENTS, orderId.toString(), mapper.writeValueAsString(new PaymentSagaResult(event, orderId, reason)))
                .get();
    }

    @Getter
    @AllArgsConstructor
    static class PaymentSagaResult {
        private KafkaBaseEvent event;
        private java.util.UUID orderId;
        private String reason;

        public UUID getEventId() {
            return event.getEventId();
        }

        public UUID getCorrelationId() {
            return event.getCorrelationId();
        }

        public String getIdempotencyKey() {
            return event.getIdempotencyKey();
        }

        public EventType getEventType() {
            return event.getEventType();
        }

        public EventSource getEventSource() {
            return event.getEventSource();
        }
    }
}
