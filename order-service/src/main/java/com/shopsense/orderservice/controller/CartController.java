package com.shopsense.orderservice.controller;

import com.shopsense.orderservice.request.AddToCartRequest;
import com.shopsense.orderservice.request.UpdateCartItemRequest;
import com.shopsense.orderservice.response.CartResponse;
import com.shopsense.orderservice.service.CartService;
import com.shopsense.orderservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.CART_URL)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem( @RequestHeader("X-User-Id") String userId, @Valid @RequestBody AddToCartRequest request ) {
        CartResponse cartResponse = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartResponse);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart( @RequestHeader("X-User-Id") String userId ) {
        CartResponse cartResponse = cartService.getCart(userId);
        return ResponseEntity.ok(cartResponse);
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem( @RequestHeader("X-User-Id") String userId, @PathVariable UUID productId, @Valid @RequestBody UpdateCartItemRequest request ) {
        CartResponse cartResponse = cartService.updateItem(userId, productId, request);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem( @RequestHeader("X-User-Id") String userId, @PathVariable UUID productId ) {
        CartResponse cartResponse = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart( @RequestHeader("X-User-Id") String userId ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent()
                .build();
    }
}