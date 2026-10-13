package com.shopsense.orderservice.service;

import com.shopsense.commonservice.enums.OrderStatus;
import com.shopsense.commonservice.request.UpdateOrderStatusRequest;
import com.shopsense.commonservice.response.PageResponse;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.response.OrderResponse;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder( UUID userId, String idempotencyKey, CreateOrderRequest request );

    OrderResponse getOrderById( UUID userId, UUID orderId );

    OrderResponse getOrderByNumber( UUID userId, String orderNumber );

    boolean isOrderExists( UUID orderId );

    PageResponse<OrderResponse> getOrders( UUID userId, PageRequest pageRequest );

    PageResponse<OrderResponse> getOrdersByStatus( UUID userId, OrderStatus status, PageRequest pageRequest );

    OrderResponse updateOrderStatus( UUID userId, UUID orderId, UpdateOrderStatusRequest request );

    void reservationFailedStatusUpdate( UUID orderId );

    void cancelOrder( UUID userId, UUID orderId );

}
