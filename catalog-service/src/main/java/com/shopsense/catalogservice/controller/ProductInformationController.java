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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppUrl.PRODUCT_URL + "/{productId}/information")
@RequiredArgsConstructor
public class ProductInformationController {

    private final ProductInformationService productInformationService;

    @PostMapping
    public ResponseEntity<ProductInformationResponse> create( @PathVariable UUID productId, @Valid @RequestBody ProductInformationRequest request ) {
        ProductInformationResponse productInformationResponse = productInformationService.create(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productInformationResponse);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductInformationResponse>> bulkCreate( @PathVariable UUID productId, @Valid @RequestBody List<@Valid ProductInformationRequest> requests ) {
        List<ProductInformationResponse> productInformationResponses = productInformationService.bulkCreate(productId, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productInformationResponses);
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<ProductInformationResponse>> getByProductId( @PathVariable UUID productId, Pageable pageable ) {
        PageResponse<ProductInformationResponse> productInformationResponse = productInformationService.getByProductId(productId, pageable);
        return ResponseEntity.ok(productInformationResponse);
    }

    @GetMapping("/{productInformationId}")
    public ResponseEntity<ProductInformationResponse> getById( @PathVariable UUID productId, @PathVariable UUID productInformationId ) {
        ProductInformationResponse productInformationResponse = productInformationService.getById(productId, productInformationId);
        return ResponseEntity.ok(productInformationResponse);
    }

    @PutMapping("/{productInformationId}")
    public ResponseEntity<ProductInformationResponse> update( @PathVariable UUID productId, @PathVariable UUID productInformationId, @Valid @RequestBody ProductInformationRequest request ) {
        ProductInformationResponse productInformationResponse = productInformationService.update(productId, productInformationId, request);
        return ResponseEntity.ok(productInformationResponse);
    }

    @DeleteMapping("/{productInformationId}")
    public ResponseEntity<Void> delete( @PathVariable UUID productId, @PathVariable UUID productInformationId ) {
        productInformationService.delete(productId, productInformationId);
        return ResponseEntity.noContent()
                .build();
    }
}