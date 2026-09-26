package com.shopsense.orderservice.service.impl;

import com.shopsense.orderservice.client.AddressServiceClient;
import com.shopsense.orderservice.client.MediaServiceClient;
import com.shopsense.orderservice.client.ProductServiceClient;
import com.shopsense.orderservice.client.StockReservationServiceClient;
import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.entity.OrderIdempotency;
import com.shopsense.orderservice.entity.OrderItem;
import com.shopsense.orderservice.entity.OrderStatusHistory;
import com.shopsense.orderservice.enums.OrderStatus;
import com.shopsense.orderservice.enums.ReservationStatus;
import com.shopsense.orderservice.exception.BadRequestException;
import com.shopsense.orderservice.exception.ResourceNotFoundException;
import com.shopsense.orderservice.mapper.OrderMapper;
import com.shopsense.orderservice.repository.OrderIdempotencyRepository;
import com.shopsense.orderservice.repository.OrderRepository;
import com.shopsense.orderservice.repository.OrderStatusHistoryRepository;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.request.OrderItemRequest;
import com.shopsense.orderservice.request.ReserveStockRequest;
import com.shopsense.orderservice.request.UpdateOrderStatusRequest;
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

    private final AddressServiceClient addressServiceClient;

    private final ProductServiceClient productServiceClient;

    private final MediaServiceClient mediaServiceClient;

    private final StockReservationServiceClient stockReservationServiceClient;

    private final com.shopsense.orderservice.client.PaymentServiceClient paymentServiceClient;

    @Override
    @Transactional
    public OrderResponse createOrder( UUID userId, String idempotencyKey, CreateOrderRequest request ) {
        OrderResponse existingOrder = findExistingOrder(userId, idempotencyKey);
        if ( existingOrder != null ) {
            return existingOrder;
        }
        validateCreateOrderRequest(userId, request);
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
            UUID productImageId = null;
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
            cartService.decreaseItemQuantity(userId, item.getProductId(), item.getQuantity());
        }
        log.info("Order created successfully: orderId={}, orderNumber={}, userId={}, status={}", savedOrder.getId(), savedOrder.getOrderNumber(), userId, savedOrder.getStatus());
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById( UUID userId, UUID orderId ) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        log.info("Order fetched successfully: orderId={}, userId={}", orderId, userId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber( UUID userId, String orderNumber ) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if ( !userId.equals(order.getUserId()) ) {
            throw new ResourceNotFoundException("Order not found");
        }
        log.info("Order fetched successfully: orderNumber={}, userId={}", orderNumber, userId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderExists( UUID orderId ) {
        if ( orderId == null ) {
            return false;
        }
        boolean exists = orderRepository.existsById(orderId);
        log.info("Order existence check completed: orderId={}, exists={}", orderId, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders( UUID userId, PageRequest pageRequest ) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageRequest);
        log.info("Orders fetched successfully: userId={}, page={}, size={}, totalElements={}", userId, pageRequest.getPageNumber(), pageRequest.getPageSize(), orderPage.getTotalElements());
        return PageResponse.from(orderPage, orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus( UUID userId, OrderStatus status, PageRequest pageRequest ) {
        if ( status == null ) {
            throw new IllegalArgumentException("Order status is required");
        }
        Page<Order> orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageRequest);
        log.info("Orders fetched by status: userId={}, status={}, page={}, size={}, totalElements={}", userId, status, pageRequest.getPageNumber(), pageRequest.getPageSize(), orderPage.getTotalElements());
        return PageResponse.from(orderPage, orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus( UUID userId, UUID orderId, UpdateOrderStatusRequest request ) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();
        validateStatusTransition(currentStatus, newStatus);
        if ( currentStatus == newStatus ) {
            return orderMapper.toResponse(order);
        }
        // Consume the hold before confirming. The inventory operation is idempotent,
        // so a retried gateway webhook can safely repeat this transition.
        if ( newStatus == OrderStatus.CONFIRMED ) {
            stockReservationServiceClient.consumeByOrderId(orderId);
        }
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.saveAndFlush(order);
        saveStatusHistory(savedOrder, currentStatus, newStatus, request.getReason(), request.getDescription());
        log.info("Order status updated: orderId={}, userId={}, fromStatus={}, toStatus={}", orderId, userId, currentStatus, newStatus);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void reservationFailedStatusUpdate( UUID orderId ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus previousStatus = order.getStatus();
        if ( previousStatus != OrderStatus.PAYMENT_PENDING && previousStatus != OrderStatus.PENDING_RESERVATION ) {
            return;
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        saveStatusHistory(savedOrder, previousStatus, OrderStatus.CANCELLED, "Order Cancelled", "Order cancelled because inventory reservation expired.");
        log.info("Order cancelled successfully: orderId={}, orderNumber={}, userId={}, previousStatus={}, status={}", orderId, savedOrder.getOrderNumber(), order.getUserId(), previousStatus, savedOrder.getStatus());
    }

    @Override
    @Transactional
    public void cancelOrder( UUID userId, UUID orderId ) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if ( order.getStatus() == OrderStatus.REFUND_PENDING ) {
            completeRefundIfReady(order, userId);
            return;
        }
        validateCancellation(order);
        OrderStatus previousStatus = order.getStatus();
        boolean paidOrder = previousStatus == OrderStatus.CONFIRMED;
        if ( paidOrder ) {
            stockReservationServiceClient.restoreConsumedByOrderId(order.getId());
        }
        StockReservationResponse reservationResponse = stockReservationServiceClient.getByOrderId(order.getId());
        if ( reservationResponse != null && reservationResponse.getItems() != null ) {
            stockReservationServiceClient.releaseByOrderId(order.getId());
        }
        order.setStatus(paidOrder ? OrderStatus.REFUND_PENDING : OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        saveStatusHistory(savedOrder, previousStatus, savedOrder.getStatus(), paidOrder ? "Refund requested" : "Order cancelled", "Order cancelled by customer");
        if ( paidOrder ) {
            completeRefundIfReady(savedOrder, userId);
        }
        log.info("Order cancelled successfully: orderId={}, orderNumber={}, userId={}, previousStatus={}, status={}", orderId, savedOrder.getOrderNumber(), userId, previousStatus, savedOrder.getStatus());
    }

    private void completeRefundIfReady( Order order, UUID userId ) {
        com.shopsense.orderservice.response.PaymentRefundResponse refund = paymentServiceClient.refundOrder(userId.toString(), order.getId());
        if ( refund != null && "REFUNDED".equalsIgnoreCase(refund.getStatus()) && order.getStatus() == OrderStatus.REFUND_PENDING ) {
            order.setStatus(OrderStatus.REFUNDED);
            Order savedOrder = orderRepository.saveAndFlush(order);
            saveStatusHistory(savedOrder, OrderStatus.REFUND_PENDING, OrderStatus.REFUNDED, "Refund completed", "Payment refund completed successfully");
        }
    }

    private void validateCreateOrderRequest( UUID userId, CreateOrderRequest request ) {
        validateAddress(userId, request.getShippingAddressId());
        Set<UUID> productIds = new HashSet<>();
        for ( OrderItemRequest item : request.getItems() ) {
            if ( !productIds.add(item.getProductId()) ) {
                throw new IllegalArgumentException("Duplicate product");
            }
        }
    }

    private void validateAddress( UUID userId, UUID addressId ) {
        boolean addressExists = addressServiceClient.isAddressExists(addressId, userId);
        if ( !addressExists ) {
            throw new ResourceNotFoundException("Address not found ");
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
                    .orElseThrow(() -> new ResourceNotFoundException("Product is not present in the cart"));
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
        if ( status == OrderStatus.REFUND_PENDING || status == OrderStatus.REFUNDED ) {
            throw new IllegalStateException("Order refund is already in progress or complete");
        }
    }

    private void validateStatusTransition( OrderStatus currentStatus, OrderStatus newStatus ) {
        boolean allowed = switch ( currentStatus ) {
            case PENDING_RESERVATION -> newStatus == OrderStatus.PAYMENT_PENDING || newStatus == OrderStatus.CANCELLED;
            case PAYMENT_PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.REFUND_PENDING;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.REFUND_PENDING;
            case DELIVERED -> newStatus == OrderStatus.REFUND_PENDING;
            case REFUND_PENDING -> newStatus == OrderStatus.REFUNDED || newStatus == OrderStatus.CANCELLED;
            case CANCELLED, REFUNDED -> false;
        };
        if ( !allowed ) {
            throw new BadRequestException("Invalid order status transition: " + currentStatus + " -> " + newStatus);
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

    private OrderResponse findExistingOrder( UUID userId, String idempotencyKey ) {
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
