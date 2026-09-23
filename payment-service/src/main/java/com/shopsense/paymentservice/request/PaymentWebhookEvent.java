package com.shopsense.paymentservice.request;


import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookEvent {

    private String eventId;

    private PaymentGateway gateway;

    private String eventType;

    private String gatewayPaymentId;

    private String gatewayOrderId;

    private String paymentMethod;

    private PaymentStatus paymentStatus;

    private String failureReason;
}