package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.ProductRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductResponse;
import com.shopsense.catalogservice.service.ProductService;
import com.shopsense.catalogservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppUrl.PRODUCT_URL)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {

        ProductResponse response =
                productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID productId
    ) {

        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getAllProducts(pageable)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByStore(
            @PathVariable UUID storeId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getProductsByStoreId(
                        storeId,
                        pageable
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getProductsByCategoryId(
                        categoryId,
                        pageable
                )
        );
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByBrand(
            @PathVariable UUID brandId,
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getProductsByBrandId(
                        brandId,
                        pageable
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<ProductResponse>> getActiveProducts(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getActiveProducts(pageable)
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRequest request
    ) {

        return ResponseEntity.ok(
                productService.updateProduct(
                        productId,
                        request
                )
        );
    }

    @PatchMapping("/{productId}/activate")
    public ResponseEntity<Void> activateProduct(
            @PathVariable UUID productId
    ) {

        productService.activateProduct(productId);

        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(
            @PathVariable UUID productId
    ) {

        productService.deactivateProduct(productId);

        return ResponseEntity.noContent()
                .build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId
    ) {

        productService.deleteProduct(productId);

        return ResponseEntity.noContent()
                .build();
    }
}
