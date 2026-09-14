package com.shopsense.inventoryservice.controller;

import com.shopsense.inventoryservice.request.CreateInventoryRequest;
import com.shopsense.inventoryservice.request.StockAdjustmentRequest;
import com.shopsense.inventoryservice.request.UpdateInventoryRequest;
import com.shopsense.inventoryservice.response.AvailabilityResponse;
import com.shopsense.inventoryservice.response.InventoryResponse;
import com.shopsense.inventoryservice.service.InventoryService;
import com.shopsense.inventoryservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.INVENTORY_URL)
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> create( @Valid @RequestBody CreateInventoryRequest request ) {
        InventoryResponse inventoryResponse = inventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryResponse);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId( @PathVariable UUID productId ) {
        InventoryResponse inventoryResponse = inventoryService.getByProductId(productId);
        return ResponseEntity.ok(inventoryResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryResponse> update( @PathVariable UUID productId, @Valid @RequestBody UpdateInventoryRequest request ) {
        InventoryResponse inventoryResponse = inventoryService.update(productId, request);
        return ResponseEntity.ok(inventoryResponse);
    }

    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock( @PathVariable UUID productId, @Valid @RequestBody StockAdjustmentRequest request ) {
        InventoryResponse inventoryResponse = inventoryService.adjustStock(productId, request);
        return ResponseEntity.ok(inventoryResponse);
    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<AvailabilityResponse> getAvailability( @PathVariable UUID productId ) {
        AvailabilityResponse availabilityResponse = inventoryService.getAvailability(productId);
        return ResponseEntity.ok(availabilityResponse);
    }
}