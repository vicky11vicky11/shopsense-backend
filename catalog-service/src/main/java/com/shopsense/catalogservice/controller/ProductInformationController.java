package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.ProductInformationRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductInformationResponse;
import com.shopsense.catalogservice.service.ProductInformationService;
import com.shopsense.catalogservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.PRODUCT_URL + "/{productId}/information")
@RequiredArgsConstructor
public class ProductInformationController {

    private final ProductInformationService productInformationService;

    @PostMapping
    public ResponseEntity<ProductInformationResponse> create( @PathVariable UUID productId, @Valid @RequestBody ProductInformationRequest request ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productInformationService.create(productId, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductInformationResponse>> getByProductId( @PathVariable UUID productId, Pageable pageable ) {
        return ResponseEntity.ok(productInformationService.getByProductId(productId, pageable));
    }

    @GetMapping("/{productInformationId}")
    public ResponseEntity<ProductInformationResponse> getById( @PathVariable UUID productId, @PathVariable UUID productInformationId ) {
        return ResponseEntity.ok(productInformationService.getById(productId, productInformationId));
    }

    @PutMapping("/{productInformationId}")
    public ResponseEntity<ProductInformationResponse> update( @PathVariable UUID productId, @PathVariable UUID productInformationId, @Valid @RequestBody ProductInformationRequest request ) {
        return ResponseEntity.ok(productInformationService.update(productId, productInformationId, request));
    }

    @DeleteMapping("/{productInformationId}")
    public ResponseEntity<Void> delete( @PathVariable UUID productId, @PathVariable UUID productInformationId ) {
        productInformationService.delete(productId, productInformationId);
        return ResponseEntity.noContent()
                .build();
    }
}