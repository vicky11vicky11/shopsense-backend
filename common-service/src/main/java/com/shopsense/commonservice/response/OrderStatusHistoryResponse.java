package com.shopsense.commonservice.response;

import com.shopsense.commonservice.enums.OrderStatus;
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