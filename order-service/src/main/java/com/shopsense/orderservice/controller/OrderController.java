package com.shopsense.orderservice.controller;

import com.shopsense.orderservice.enums.OrderStatus;
import com.shopsense.orderservice.request.CreateOrderRequest;
import com.shopsense.orderservice.response.OrderResponse;
import com.shopsense.orderservice.response.PageResponse;
import com.shopsense.orderservice.service.OrderService;
import com.shopsense.orderservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.ORDER_URL)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder( @RequestHeader("X-User-Id") String userId, @Valid @RequestBody CreateOrderRequest request ) {
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById( @RequestHeader("X-User-Id") String userId, @PathVariable UUID orderId ) {
        OrderResponse orderResponse = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber( @RequestHeader("X-User-Id") String userId, @PathVariable String orderNumber ) {
        OrderResponse orderResponse = orderService.getOrderByNumber(userId, orderNumber);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/exists/{orderId}")
    public ResponseEntity<Boolean> getOrderExists( @PathVariable UUID orderId ) {
        boolean isOrderExists = orderService.isOrderExists(orderId);
        return ResponseEntity.ok(isOrderExists);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders( @RequestHeader("X-User-Id") String userId, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) PageRequest pageRequest ) {
        PageResponse<OrderResponse> orderResponse = orderService.getOrders(userId, pageRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByStatus( @RequestHeader("X-User-Id") String userId, @PathVariable OrderStatus status, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) PageRequest pageRequest ) {
        PageResponse<OrderResponse> orderResponse = orderService.getOrdersByStatus(userId, status, pageRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder( @RequestHeader("X-User-Id") String userId, @PathVariable UUID orderId ) {
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.noContent()
                .build();
    }
}