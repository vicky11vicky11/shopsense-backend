package com.shopsense.paymentservice.client;

import com.shopsense.commonservice.request.UpdateOrderStatusRequest;
import com.shopsense.commonservice.util.AppUrl;
import com.shopsense.paymentservice.response.ProductOrderResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;

import java.util.UUID;

@HttpExchange(AppUrl.ORDER_URL)
public interface OrderServiceClient {

    @GetExchange("/{orderId}")
    ProductOrderResponse getOrderById( @RequestHeader("X-User-Id") UUID userId, @PathVariable UUID orderId );

    @PatchExchange("/{orderId}/status")
    ProductOrderResponse updateOrderStatus( @RequestHeader("X-User-Id") UUID userId, @PathVariable UUID orderId, @RequestBody UpdateOrderStatusRequest request );
}
