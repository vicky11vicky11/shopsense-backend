package com.shopsense.orderservice.response;

import com.shopsense.orderservice.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {

    private UUID id;

    private String orderNumber;

    private String userId;

    private OrderStatus status;

    private BigDecimal subtotal;

    private BigDecimal discountAmount;

    private BigDecimal shippingAmount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String currency;

    private String shippingAddressId;

    private List<OrderItemResponse> items;

    private List<OrderStatusHistoryResponse> statusHistory;

    private Instant createdAt;

    private Instant updatedAt;
}