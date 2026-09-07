package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.CreateStoreRequest;
import com.shopsense.catalogservice.request.UpdateStoreRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.StoreResponse;
import com.shopsense.catalogservice.service.StoreService;
import com.shopsense.catalogservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppUrl.STORE_URL)
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore( @Valid @RequestBody CreateStoreRequest request ) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById( @PathVariable UUID storeId ) {
        return ResponseEntity.ok(storeService.getStoreById(storeId));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<PageResponse<StoreResponse>> getStoresBySellerId( @PathVariable String sellerId, @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable ) {
        return ResponseEntity.ok(storeService.getStoresBySellerId(sellerId, pageable));
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore( @PathVariable UUID storeId, @Valid @RequestBody UpdateStoreRequest request ) {
        return ResponseEntity.ok(storeService.updateStore(storeId, request));
    }

    @PatchMapping("/{storeId}/activate")
    public ResponseEntity<Void> activateStore( @PathVariable UUID storeId ) {
        storeService.activateStore(storeId);
        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/{storeId}/deactivate")
    public ResponseEntity<Void> deactivateStore( @PathVariable UUID storeId ) {
        storeService.deactivateStore(storeId);
        return ResponseEntity.noContent()
                .build();
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore( @PathVariable UUID storeId ) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent()
                .build();
    }
}