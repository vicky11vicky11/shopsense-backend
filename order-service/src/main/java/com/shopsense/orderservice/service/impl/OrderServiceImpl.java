package com.shopsense.orderservice.service.impl;

import com.shopsense.orderservice.client.AddressServiceClient;
import com.shopsense.orderservice.client.CustomerServiceClient;
import com.shopsense.orderservice.client.ProductServiceClient;
import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.entity.OrderItem;
import com.shopsense.orderservice.enums.OrderStatus;
import com.shopsense.orderservice.exceptions.BadRequestException;
import com.shopsense.orderservice.exceptions.ResourceNotFoundException;
import com.shopsense.orderservice.mapper.OrderMapper;
import com.shopsense.orderservice.repository.OrderRepository;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.request.OrderItemRequest;
import com.shopsense.orderservice.response.CartItemResponse;
import com.shopsense.orderservice.response.CartResponse;
import com.shopsense.orderservice.response.OrderResponse;
import com.shopsense.orderservice.response.PageResponse;
import com.shopsense.orderservice.service.CartService;
import com.shopsense.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CartService cartService;

    private final OrderMapper orderMapper;

    private final CustomerServiceClient customerServiceClient;

    private final AddressServiceClient addressServiceClient;

    private final ProductServiceClient productServiceClient;

    @Override
    public OrderResponse createOrder( String userId, CreateOrderRequest request ) {
        validateUser(userId);
        validateCreateOrderRequest(request);
        CartResponse cartResponse = cartService.getCart(userId);
        validateCartItems(cartResponse, request.getItems());
        /*
         * TODO:
         *
         * 1. Get the user's cart.
         * 2. Validate that every requested product exists in the cart.
         * 3. Validate requested quantity <= cart quantity.
         * 4. Validate customer/user.
         * 5. Fetch products from catalog service.
         * 6. Validate products are active.
         * 7. Get current product prices from catalog.
         * 8. Reserve inventory.
         *
         * Since you are handling inter-service communication,
         * those calls can be added here.
         */
        String orderNumber = generateOrderNumber();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        /*
         * For now, these values are placeholders until catalog/
         * pricing/shipping/tax communication is implemented.
         */
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .status(OrderStatus.PENDING_RESERVATION)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingAmount(shippingAmount)
                .taxAmount(taxAmount)
                .totalAmount(subtotal.subtract(discountAmount)
                        .add(shippingAmount)
                        .add(taxAmount))
                .currency("INR")
                .shippingAddressId(request.getShippingAddressId())
                .build();
        /*
         * Create OrderItems.
         *
         * Product name, price, image etc. should eventually come
         * from Catalog Service rather than the request.
         */
        for ( OrderItemRequest itemRequest : request.getItems() ) {
            /*
             * TODO:
             * Replace these values with Catalog Service response.
             */
            BigDecimal unitPrice = BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName("Product")
                    .productImageUrl(null)
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .lineTotal(lineTotal)
                    .currency("INR")
                    .build();
            order.addItem(orderItem);
            subtotal = subtotal.add(lineTotal);
        }
        /*
         * Recalculate totals after adding items.
         */
        order.setSubtotal(subtotal);

        order.setTotalAmount(subtotal.subtract(discountAmount)
                .add(shippingAmount)
                .add(taxAmount));
        /*
         * TODO:
         *
         * Reserve inventory here.
         *
         * If reservation fails:
         *   - do not create/keep the order
         *   - throw appropriate exception
         *
         * If successful:
         *   order.setInventoryReservationId(reservationId);
         */

        order.setStatus(OrderStatus.PAYMENT_PENDING);

        Order savedOrder = orderRepository.save(order);

        /*
         * TODO:
         *
         * Remove ONLY the selected products from the cart.
         *
         * Do NOT clear the entire cart because this supports
         * partial checkout.
         *
         * Example:
         *
         * Cart:
         * A x2
         * B x1
         * C x3
         * D x1
         *
         * Request:
         * A x2
         * C x3
         *
         * Remaining cart:
         * B x1
         * D x1
         */

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById( String userId, UUID orderId ) {
        validateUser(userId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber( String userId, String orderNumber ) {
        validateUser(userId);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        if ( !userId.equals(order.getUserId()) ) {
            throw new ResourceNotFoundException("Order not found with order number: " + orderNumber);
        }
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderExists( UUID orderId ) {
        if ( orderId == null ) {
            return false;
        }
        return orderRepository.existsById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders( String userId, PageRequest pageRequest ) {

        validateUser(userId);

        Page<Order> orderPage = orderRepository.findByUserId(userId, pageRequest);

        return PageResponse.from(orderPage, orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus( String userId, OrderStatus status, PageRequest pageRequest ) {

        validateUser(userId);

        if ( status == null ) {
            throw new IllegalArgumentException("Order status is required");
        }

        Page<Order> orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageRequest);

        return PageResponse.from(orderPage, orderMapper::toResponse);
    }

    @Override
    public void cancelOrder( String userId, UUID orderId ) {
        validateUser(userId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        validateCancellation(order);
        /*
         * TODO:
         *
         * If inventory was reserved:
         *
         * inventoryService.releaseReservation(
         *      order.getInventoryReservationId()
         * );
         */
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateCreateOrderRequest( CreateOrderRequest request ) {
        if ( request == null ) {
            throw new IllegalArgumentException("Create order request is required");
        }
        if ( request.getItems() == null || request.getItems()
                .isEmpty() ) {
            throw new IllegalArgumentException("At least one item is required");
        }
        if ( request.getShippingAddressId() == null || request.getShippingAddressId()
                .trim()
                .isEmpty() ) {
            throw new IllegalArgumentException("Shipping address ID is required");
        }
        validateAddress(request.getShippingAddressId());
        Set<UUID> productIds = new HashSet<>();
        for ( OrderItemRequest item : request.getItems() ) {
            if ( item == null ) {
                throw new IllegalArgumentException("Order item cannot be null");
            }
            if ( item.getProductId() == null ) {
                throw new IllegalArgumentException("Product ID is required");
            }
            validateProduct(item.getProductId());
            if ( item.getQuantity() == null || item.getQuantity() <= 0 ) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if ( !productIds.add(item.getProductId()) ) {
                throw new IllegalArgumentException("Duplicate product in order: " + item.getProductId());
            }
        }
    }

    private void validateUser( String userId ) {
        boolean customerExists = customerServiceClient.isCustomerExists(userId);
        if ( !customerExists ) {
            throw new ResourceNotFoundException("Customer not found with id: " + userId);
        }
    }

    private void validateAddress( String addressId ) {
        boolean addressExists = addressServiceClient.isAddressExists(addressId);
        if ( !addressExists ) {
            throw new ResourceNotFoundException("Address not found with id: " + addressId);
        }
    }

    private void validateProduct( UUID productId ) {
        boolean productExists = productServiceClient.isProductExists(productId);
        if ( !productExists ) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    private void validateCartItems( CartResponse cart, List<OrderItemRequest> requestedItems ) {
        if ( cart == null || cart.getItems() == null || cart.getItems()
                .isEmpty() ) {
            throw new BadRequestException("Cart is empty");
        }
        for ( OrderItemRequest requestedItem : requestedItems ) {
            CartItemResponse cartItem = cart.getItems()
                    .stream()
                    .filter(item -> item.getProductId()
                            .equals(requestedItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Product is not present in the cart: " + requestedItem.getProductId()));
            if ( requestedItem.getQuantity() > cartItem.getQuantity() ) {
                throw new BadRequestException("Requested quantity for product " + requestedItem.getProductId() + " exceeds cart quantity");
            }
        }
    }

    private void validateCancellation( Order order ) {
        OrderStatus status = order.getStatus();
        if ( status == OrderStatus.CANCELLED ) {
            throw new IllegalStateException("Order is already cancelled");
        }
        if ( status == OrderStatus.SHIPPED ) {
            throw new IllegalStateException("Shipped order cannot be cancelled");
        }
        if ( status == OrderStatus.DELIVERED ) {
            throw new IllegalStateException("Delivered order cannot be cancelled");
        }
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
        return "ORD-" + timestamp.substring(timestamp.length() - 10) + "-" + random;
    }
}