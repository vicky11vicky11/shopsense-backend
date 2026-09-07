package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.BrandRequest;
import com.shopsense.catalogservice.response.BrandResponse;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.service.BrandService;
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
@RequestMapping(AppUrl.BRAND_URL)
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand( @Valid @RequestBody BrandRequest request ) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<BrandResponse> getBrandById( @PathVariable UUID brandId ) {
        return ResponseEntity.ok(brandService.getBrandById(brandId));
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<BrandResponse>> getAllBrands( @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable ) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<BrandResponse>> getActiveBrands( @PageableDefault(size = 10, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable ) {
        return ResponseEntity.ok(brandService.getActiveBrands(pageable));
    }

    @PutMapping("/{brandId}")
    public ResponseEntity<BrandResponse> updateBrand( @PathVariable UUID brandId, @Valid @RequestBody BrandRequest request ) {
        return ResponseEntity.ok(brandService.updateBrand(brandId, request));
    }

    @PatchMapping("/{brandId}/activate")
    public ResponseEntity<Void> activateBrand( @PathVariable UUID brandId ) {
        brandService.activateBrand(brandId);
        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/{brandId}/deactivate")
    public ResponseEntity<Void> deactivateBrand( @PathVariable UUID brandId ) {
        brandService.deactivateBrand(brandId);
        return ResponseEntity.noContent()
                .build();
    }

    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand( @PathVariable UUID brandId ) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent()
                .build();
    }
}