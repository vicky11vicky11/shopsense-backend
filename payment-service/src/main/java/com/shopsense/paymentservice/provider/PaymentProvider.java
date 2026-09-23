package com.shopsense.paymentservice.provider;


import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.request.CreateGatewayPaymentRequest;
import com.shopsense.paymentservice.request.GatewayRefundRequest;
import com.shopsense.paymentservice.request.PaymentWebhookEvent;
import com.shopsense.paymentservice.request.VerifyPaymentRequest;
import com.shopsense.paymentservice.response.GatewayPaymentResponse;
import com.shopsense.paymentservice.response.GatewayRefundResponse;

public interface PaymentProvider {

    PaymentGateway getGateway();

    GatewayPaymentResponse createPayment( CreateGatewayPaymentRequest request );

    boolean verifyPayment( VerifyPaymentRequest request );

    GatewayRefundResponse refund( GatewayRefundRequest request );

    PaymentWebhookEvent parseWebhook( String payload, String signature, String eventId );
}