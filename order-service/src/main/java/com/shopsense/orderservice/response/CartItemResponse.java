package com.shopsense.orderservice.response;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CartItemResponse {

    private UUID productId;

    private Integer quantity;
}