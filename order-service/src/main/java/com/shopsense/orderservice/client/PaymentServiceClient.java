package com.shopsense.orderservice.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.shopsense.orderservice.response.PaymentRefundResponse;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@HttpExchange("/api/v1/payments")
public interface PaymentServiceClient {

    @PostExchange("/order/{orderId}/refund")
    PaymentRefundResponse refundOrder(@RequestHeader("X-User-Id") String userId, @PathVariable UUID orderId);

}
