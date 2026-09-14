package com.shopsense.inventoryservice.controller;

import com.shopsense.inventoryservice.request.ReserveStockRequest;
import com.shopsense.inventoryservice.response.StockReservationResponse;
import com.shopsense.inventoryservice.service.StockReservationService;
import com.shopsense.inventoryservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

        return ResponseEntity.ok(reservationService.reserve(request));
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
}