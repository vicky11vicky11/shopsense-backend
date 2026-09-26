package com.shopsense.orderservice.service.impl;

import com.shopsense.orderservice.client.ProductServiceClient;
import com.shopsense.orderservice.entity.CartItem;
import com.shopsense.orderservice.exception.ResourceNotFoundException;
import com.shopsense.orderservice.request.AddToCartRequest;
import com.shopsense.orderservice.request.UpdateCartItemRequest;
import com.shopsense.orderservice.response.CartItemResponse;
import com.shopsense.orderservice.response.CartResponse;
import com.shopsense.orderservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    @Value("${order.cart-key-prefix}")
    private String cartKeyPrefix;

    @Value("${order.cart-ttl-days}")
    private Integer cartTtlDays;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private final ProductServiceClient productServiceClient;

    @Override
    public CartResponse addItem( UUID userId, AddToCartRequest request ) {
        String cartKey = getCartKey(userId);
        validateProduct(request.getProductId());
        CartItem existingItem = getCartItem(cartKey, request.getProductId());
        int quantity = request.getQuantity();
        if ( existingItem != null ) {
            log.debug("Product already exists in cart: userId={}, productId={}, existingQuantity={}", userId, request.getProductId(), existingItem.getQuantity());
            quantity += existingItem.getQuantity();
        }
        CartItem cartItem = CartItem.builder()
                .productId(request.getProductId())
                .quantity(quantity)
                .build();
        redisTemplate.opsForHash()
                .put(cartKey, request.getProductId()
                        .toString(), cartItem);
        refreshCartTtl(cartKey);
        log.info("Item added to cart successfully: userId={}, productId={}, finalQuantity={}", userId, request.getProductId(), quantity);
        return getCart(userId);
    }

    @Override
    public CartResponse getCart( UUID userId ) {
        String cartKey = getCartKey(userId);
        List<CartItem> items = getCartItems(cartKey);
        log.debug("Cart fetched successfully: userId={}, cartKey={}, itemCount={}", userId, cartKey, items.size());
        return buildCartResponse(userId, items);
    }

    @Override
    public CartResponse updateItem( UUID userId, UUID productId, UpdateCartItemRequest request ) {
        String cartKey = getCartKey(userId);
        CartItem existingItem = getCartItem(cartKey, productId);
        if ( existingItem == null ) {
            throw new ResourceNotFoundException("Product is not present in the cart");
        }
        CartItem cartItem = CartItem.builder()
                .productId(productId)
                .quantity(request.getQuantity())
                .build();
        redisTemplate.opsForHash()
                .put(cartKey, productId.toString(), cartItem);
        refreshCartTtl(cartKey);
        log.info("Cart item updated successfully: userId={}, productId={}, quantity={}", userId, productId, request.getQuantity());
        return getCart(userId);
    }

    @Override
    public void decreaseItemQuantity( UUID userId, UUID productId, int quantity ) {
        if ( quantity <= 0 ) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        String cartKey = getCartKey(userId);
        CartItem existingItem = getCartItem(cartKey, productId);
        if ( existingItem == null ) {
            throw new ResourceNotFoundException("Product is not present in the cart");
        }
        int currentQuantity = existingItem.getQuantity();
        if ( quantity > currentQuantity ) {
            throw new IllegalArgumentException("Requested quantity exceeds cart quantity");
        }
        int remainingQuantity = currentQuantity - quantity;
        if ( remainingQuantity == 0 ) {
            redisTemplate.opsForHash()
                    .delete(cartKey, productId.toString());
            log.info("Cart item removed after checkout: userId={}, productId={}", userId, productId);
        } else {
            CartItem updatedItem = CartItem.builder()
                    .productId(productId)
                    .quantity(remainingQuantity)
                    .build();
            redisTemplate.opsForHash()
                    .put(cartKey, productId.toString(), updatedItem);
            log.info("Cart item quantity decreased: userId={}, productId={}, oldQuantity={}, decreasedBy={}, remainingQuantity={}", userId, productId, currentQuantity, quantity, remainingQuantity);
        }
        refreshCartTtl(cartKey);
    }

    @Override
    public void removeItem( UUID userId, UUID productId ) {
        String cartKey = getCartKey(userId);
        CartItem existingItem = getCartItem(cartKey, productId);
        if ( existingItem == null ) {
            throw new ResourceNotFoundException("Product is not present in the cart");
        }
        redisTemplate.opsForHash()
                .delete(cartKey, productId.toString());
        refreshCartTtl(cartKey);
        log.info("Cart item removed successfully: userId={}, productId={}", userId, productId);
    }

    @Override
    public void clearCart( UUID userId ) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
        log.info("Cart cleared successfully: userId={}, cartKey={}", userId, cartKey);
    }

    private CartItem getCartItem( String cartKey, UUID productId ) {
        Object value = redisTemplate.opsForHash()
                .get(cartKey, productId.toString());
        if ( value == null ) {
            return null;
        }
        return objectMapper.convertValue(value, CartItem.class);
    }

    private List<CartItem> getCartItems( String cartKey ) {
        List<Object> values = redisTemplate.opsForHash()
                .values(cartKey);
        List<CartItem> items = new ArrayList<>();
        for ( Object value : values ) {
            items.add(objectMapper.convertValue(value, CartItem.class));
        }
        return items;
    }

    private CartResponse buildCartResponse( UUID userId, List<CartItem> items ) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(totalItems)
                .build();
    }

    private String getCartKey( UUID userId ) {
        return cartKeyPrefix + userId;
    }

    private void refreshCartTtl( String cartKey ) {
        Duration duration = Duration.ofDays(cartTtlDays);
        redisTemplate.expire(cartKey, duration);
    }

    private void validateProduct( UUID productId ) {
        boolean productExists = productServiceClient.isProductExists(productId);
        if ( !productExists ) {
            throw new ResourceNotFoundException("Product does not exist");
        }
    }
}