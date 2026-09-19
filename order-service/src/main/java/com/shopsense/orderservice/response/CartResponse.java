package com.shopsense.orderservice.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartResponse {

    private String userId;

    private List<CartItemResponse> items;

    private Integer totalItems;
}