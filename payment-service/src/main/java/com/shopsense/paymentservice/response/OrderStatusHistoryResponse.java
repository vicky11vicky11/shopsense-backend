package com.shopsense.paymentservice.response;

import com.shopsense.paymentservice.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class OrderStatusHistoryResponse {

    private UUID id;

    private OrderStatus fromStatus;

    private OrderStatus toStatus;

    private String reason;

    private String description;

    private Instant createdAt;
}