package com.shopsense.paymentservice.service;

import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.request.CreatePaymentRequest;
import com.shopsense.paymentservice.response.PageResponse;
import com.shopsense.paymentservice.response.PaymentResponse;
import com.shopsense.paymentservice.response.RefundResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment( UUID userId, CreatePaymentRequest request );

    PaymentResponse verifyRazorpayPayment( UUID userId, UUID paymentId, String gatewayOrderId, String gatewayPaymentId, String signature );

    PageResponse<PaymentResponse> getAllPayments( Pageable pageable );

    PaymentResponse getPayment( UUID userId, UUID paymentId );

    PaymentResponse getPaymentByOrderId( UUID userId, UUID orderId );

    RefundResponse refundPayment( UUID userId, UUID paymentId, BigDecimal amount, String reason );

    RefundResponse refundOrder( UUID userId, UUID orderId );

    void handleWebhook( PaymentGateway gateway, String payload, String signature, String eventId );

}
