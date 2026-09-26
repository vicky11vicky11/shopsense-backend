package com.shopsense.orderservice.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartResponse {

    private UUID userId;

    private List<CartItemResponse> items;

    private Integer totalItems;
}