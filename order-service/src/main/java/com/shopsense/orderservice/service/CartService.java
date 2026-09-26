package com.shopsense.orderservice.service;

import com.shopsense.orderservice.request.AddToCartRequest;
import com.shopsense.orderservice.request.UpdateCartItemRequest;
import com.shopsense.orderservice.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse addItem( UUID userId, AddToCartRequest request );

    CartResponse getCart( UUID userId );

    CartResponse updateItem( UUID userId, UUID productId, UpdateCartItemRequest request );

    void decreaseItemQuantity( UUID userId, UUID productId, int quantity );

    void removeItem( UUID userId, UUID productId );

    void clearCart( UUID userId );
}