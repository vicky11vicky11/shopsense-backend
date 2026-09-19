package com.shopsense.orderservice.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderItemResponse {

    private UUID id;

    private UUID productId;

    private String productName;

    private String productImageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal lineTotal;

    private String currency;
}