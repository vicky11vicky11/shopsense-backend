package com.shopsense.orderservice.service;

import com.shopsense.orderservice.request.AddToCartRequest;
import com.shopsense.orderservice.request.UpdateCartItemRequest;
import com.shopsense.orderservice.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart( String userId );

    CartResponse addItem( String userId, AddToCartRequest request );

    CartResponse updateItem( String userId, UUID productId, UpdateCartItemRequest request );

    CartResponse removeItem( String userId, UUID productId );

    void clearCart( String userId );
}