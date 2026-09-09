package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.ProductMediaRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductMediaResponse;
import com.shopsense.catalogservice.service.ProductMediaService;
import com.shopsense.catalogservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(AppUrl.PRODUCT_URL + "/{productId}/media")
@RequiredArgsConstructor
public class ProductMediaController {

    private final ProductMediaService productMediaService;

    @PostMapping
    public ResponseEntity<ProductMediaResponse> createProductMedia( @PathVariable UUID productId, @Valid @RequestBody ProductMediaRequest request ) {
        ProductMediaResponse response = productMediaService.create(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductMediaResponse>> createBulkProductMedia( @PathVariable UUID productId, @Valid @RequestBody List<ProductMediaRequest> requests ) {
        List<ProductMediaResponse> response = productMediaService.createBulk(productId, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<ProductMediaResponse>> getByProductId( @PathVariable UUID productId, Pageable pageable ) {
        PageResponse<ProductMediaResponse> productMediaResponse = productMediaService.getByProductId(productId, pageable);
        return ResponseEntity.ok(productMediaResponse);
    }

    @GetMapping("/{productMediaId}")
    public ResponseEntity<ProductMediaResponse> getById( @PathVariable UUID productId, @PathVariable UUID productMediaId ) {
        ProductMediaResponse productMediaResponse = productMediaService.getById(productId, productMediaId);
        return ResponseEntity.ok(productMediaResponse);
    }

    @PutMapping("/{productMediaId}")
    public ResponseEntity<ProductMediaResponse> update( @PathVariable UUID productId, @PathVariable UUID productMediaId, @Valid @RequestBody ProductMediaRequest request ) {
        ProductMediaResponse productMediaResponse = productMediaService.update(productId, productMediaId, request);
        return ResponseEntity.ok(productMediaResponse);
    }

    @PatchMapping("/{productMediaId}/primary")
    public ResponseEntity<ProductMediaResponse> setPrimaryImage( @PathVariable UUID productId, @PathVariable UUID productMediaId ) {
        ProductMediaResponse productMediaResponse = productMediaService.setPrimaryImage(productId, productMediaId);
        return ResponseEntity.ok(productMediaResponse);
    }

    @DeleteMapping("/{productMediaId}")
    public ResponseEntity<Void> delete( @PathVariable UUID productId, @PathVariable UUID productMediaId ) {
        productMediaService.delete(productId, productMediaId);
        return ResponseEntity.noContent()
                .build();
    }
}
