package com.shopsense.paymentservice.service;

import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.request.CreatePaymentRequest;
import com.shopsense.paymentservice.response.PaymentResponse;
import com.shopsense.paymentservice.response.RefundResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment( String userId, CreatePaymentRequest request );

    PaymentResponse verifyRazorpayPayment( String userId, UUID paymentId, String gatewayOrderId, String gatewayPaymentId, String signature );

    PaymentResponse getPayment( String userId, UUID paymentId );

    PaymentResponse getPaymentByOrderId( String userId, UUID orderId );

    RefundResponse refundPayment( String userId, UUID paymentId, BigDecimal amount, String reason );

    void handleWebhook( PaymentGateway gateway, String payload, String signature, String eventId );
}