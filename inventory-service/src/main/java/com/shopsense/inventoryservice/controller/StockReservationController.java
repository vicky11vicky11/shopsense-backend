package com.shopsense.inventoryservice.controller;

import com.shopsense.inventoryservice.request.ReserveStockRequest;
import com.shopsense.inventoryservice.response.StockReservationResponse;
import com.shopsense.inventoryservice.service.StockReservationService;
import com.shopsense.inventoryservice.util.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.RESERVATION_URL)
@RequiredArgsConstructor
public class StockReservationController {

    private final StockReservationService reservationService;

    @PostMapping
    public ResponseEntity<StockReservationResponse> reserve( @Valid @RequestBody ReserveStockRequest request ) {
        StockReservationResponse response = reservationService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<StockReservationResponse> getById( @PathVariable UUID reservationId ) {
        StockReservationResponse response = reservationService.getById(reservationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<StockReservationResponse> getByOrderId( @PathVariable UUID orderId ) {
        StockReservationResponse response = reservationService.getByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/release")
    public ResponseEntity<Void> release( @PathVariable UUID reservationId ) {
        reservationService.release(reservationId);
        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/{reservationId}/consume")
    public ResponseEntity<Void> consume( @PathVariable UUID reservationId ) {
        reservationService.consume(reservationId);
        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/order/{orderId}/release")
    public ResponseEntity<Void> releaseByOrder( @PathVariable UUID orderId ) {
        reservationService.releaseByOrder(orderId);
        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/order/{orderId}/consume")
    public ResponseEntity<Void> consumeByOrder( @PathVariable UUID orderId ) {
        reservationService.consumeByOrder(orderId);
        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/order/{orderId}/restore")
    public ResponseEntity<Void> restoreConsumedByOrder( @PathVariable UUID orderId ) {
        reservationService.restoreConsumedByOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
