package com.shopsense.orderservice.service.impl;

import com.shopsense.orderservice.client.*;
import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.entity.OrderIdempotency;
import com.shopsense.orderservice.entity.OrderItem;
import com.shopsense.orderservice.entity.OrderStatusHistory;
import com.shopsense.orderservice.enums.OrderStatus;
import com.shopsense.orderservice.enums.ReservationStatus;
import com.shopsense.orderservice.exceptions.BadRequestException;
import com.shopsense.orderservice.exceptions.ResourceNotFoundException;
import com.shopsense.orderservice.mapper.OrderMapper;
import com.shopsense.orderservice.repository.OrderIdempotencyRepository;
import com.shopsense.orderservice.repository.OrderRepository;
import com.shopsense.orderservice.repository.OrderStatusHistoryRepository;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.request.OrderItemRequest;
import com.shopsense.orderservice.request.ReserveStockRequest;
import com.shopsense.orderservice.response.*;
import com.shopsense.orderservice.service.CartService;
import com.shopsense.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    @Value("${order.tax-rate}")
    private BigDecimal taxRate;

    @Value("${order.shipping-rate}")
    private BigDecimal shippingRate;

    private final OrderRepository orderRepository;

    private final OrderIdempotencyRepository orderIdempotencyRepository;

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final CartService cartService;

    private final OrderMapper orderMapper;

    private final CustomerServiceClient customerServiceClient;

    private final AddressServiceClient addressServiceClient;

    private final ProductServiceClient productServiceClient;

    private final MediaServiceClient mediaServiceClient;

    private final StockReservationServiceClient stockReservationServiceClient;

    @Override
    public OrderResponse createOrder( String userId, String idempotencyKey, CreateOrderRequest request ) {
        validateUser(userId);
        OrderResponse existingOrder = findExistingOrder(userId, idempotencyKey);
        if (existingOrder != null) {
            return existingOrder;
        }
        validateCreateOrderRequest(request);
        CartResponse cartResponse = cartService.getCart(userId);
        validateCartItems(cartResponse, request.getItems());
        String orderNumber = generateOrderNumber();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .status(OrderStatus.PENDING_RESERVATION)
                .subtotal(BigDecimal.ZERO)
                .discountAmount(discountAmount)
                .shippingAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .shippingAddressId(request.getShippingAddressId())
                .build();
        for ( OrderItemRequest itemRequest : request.getItems() ) {
            ProductResponse productResponse = productServiceClient.getProduct(itemRequest.getProductId());
            String productImageId = null;
            String productImageUrl = null;
            if ( productResponse.getProductImages() != null && !productResponse.getProductImages()
                    .isEmpty() ) {
                productImageId = productResponse.getProductImages()
                        .stream()
                        .filter(ProductMediaResponse::isPrimaryImage)
                        .map(ProductMediaResponse::getMediaId)
                        .findFirst()
                        .orElse(productResponse.getProductImages()
                                .getFirst()
                                .getMediaId());
            }
            if ( productImageId != null ) {
                productImageUrl = mediaServiceClient.getMedia(productImageId)
                        .getSecureUrl();
            }
            BigDecimal unitPrice = productResponse.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .productId(productResponse.getId())
                    .productName(productResponse.getName())
                    .productImageUrl(productImageUrl)
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .lineTotal(lineTotal)
                    .currency(request.getCurrency())
                    .build();
            order.addItem(orderItem);
            subtotal = subtotal.add(lineTotal);
        }
        BigDecimal taxRate = getTaxRate();
        BigDecimal shippingRate = getShippingRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingAmount = subtotal.multiply(shippingRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.subtract(discountAmount)
                .add(shippingAmount)
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));
        order.setShippingAmount(shippingAmount);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_RESERVATION);
        Order savedOrder = orderRepository.saveAndFlush(order);
        saveStatusHistory(savedOrder, null, OrderStatus.PENDING_RESERVATION, "Order created", "Order created and waiting for stock reservation");
        ReserveStockRequest reservationRequest = ReserveStockRequest.builder()
                .orderId(savedOrder.getId())
                .items(request.getItems()
                        .stream()
                        .map(item -> ReserveStockRequest.ReservationItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        StockReservationResponse stockReservationResponse = stockReservationServiceClient.reserve(reservationRequest);
        if ( !ReservationStatus.RESERVED.equals(stockReservationResponse.getStatus()) ) {
            throw new BadRequestException("Unable to reserve stock for the order");
        }
        OrderStatus previousStatus = savedOrder.getStatus();
        savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        savedOrder = orderRepository.saveAndFlush(savedOrder);
        saveStatusHistory(savedOrder, previousStatus, OrderStatus.PAYMENT_PENDING, "Stock reserved", "Inventory stock successfully reserved for the order");
        OrderIdempotency idempotency = OrderIdempotency.builder()
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .order(savedOrder)
                .build();
        orderIdempotencyRepository.save(idempotency);
        for ( OrderItemRequest item : request.getItems() ) {
            cartService.removeItem(userId, item.getProductId());
        }
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
        OrderStatus previousStatus = order.getStatus();
        StockReservationResponse reservationResponse = stockReservationServiceClient.getByOrderId(order.getId());
        if ( reservationResponse != null && reservationResponse.getItems() != null ) {
            for ( StockReservationResponse.ReservationItemResponse item : reservationResponse.getItems() ) {
                stockReservationServiceClient.release(item.getReservationId());
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        saveStatusHistory(savedOrder, previousStatus, OrderStatus.CANCELLED, "Order cancelled", "Order cancelled by customer");
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

    private BigDecimal getTaxRate() {
        return taxRate.movePointLeft(2);
    }

    private BigDecimal getShippingRate() {
        return shippingRate.movePointLeft(2);
    }

    private OrderResponse findExistingOrder( String userId, String idempotencyKey ) {
        if ( idempotencyKey == null || idempotencyKey.isBlank() ) {
            throw new BadRequestException("Idempotency-Key is required");
        }
        return orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(OrderIdempotency::getOrder)
                .map(orderMapper::toResponse)
                .orElse(null);
    }

    private void saveStatusHistory( Order order, OrderStatus fromStatus, OrderStatus toStatus, String reason, String description ) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reason(reason)
                .description(description)
                .build();
        orderStatusHistoryRepository.save(history);
    }
}