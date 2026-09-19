package com.shopsense.orderservice.service;

import com.shopsense.orderservice.enums.OrderStatus;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.response.OrderResponse;
import com.shopsense.orderservice.response.PageResponse;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder( String userId, CreateOrderRequest request );

    OrderResponse getOrderById( String userId, UUID orderId );

    OrderResponse getOrderByNumber( String userId, String orderNumber );

    boolean isOrderExists( UUID orderId );

    PageResponse<OrderResponse> getOrders( String userId, PageRequest pageRequest );

    PageResponse<OrderResponse> getOrdersByStatus( String userId, OrderStatus status, PageRequest pageRequest );

    void cancelOrder( String userId, UUID orderId );
}
