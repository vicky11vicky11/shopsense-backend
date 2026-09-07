package com.shopsense.catalogservice.controller;

import com.shopsense.catalogservice.request.CategoryRequest;
import com.shopsense.catalogservice.response.CategoryResponse;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.service.CategoryService;
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
@RequestMapping(AppUrl.CATEGORY_URL)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory( @Valid @RequestBody CategoryRequest request ) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById( @PathVariable UUID categoryId ) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/bulk")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories( @PageableDefault(size = 10, page = 0, sort = "categoryName", direction = Sort.Direction.ASC) Pageable pageable ) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<CategoryResponse>> getActiveCategories( @PageableDefault(size = 10, page = 0, sort = "categoryName", direction = Sort.Direction.ASC) Pageable pageable ) {
        return ResponseEntity.ok(categoryService.getActiveCategories(pageable));
    }

    @GetMapping("/root")
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories( @PageableDefault(size = 10, page = 0, sort = "categoryName", direction = Sort.Direction.ASC) Pageable pageable ) {
        return ResponseEntity.ok(categoryService.getRootCategories(pageable));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories( @PathVariable UUID categoryId, @PageableDefault(size = 10, page = 0, sort = "categoryName", direction = Sort.Direction.ASC) Pageable pageable ) {
        return ResponseEntity.ok(categoryService.getSubCategories(categoryId, pageable));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory( @PathVariable UUID categoryId, @Valid @RequestBody CategoryRequest request ) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @PatchMapping("/{categoryId}/activate")
    public ResponseEntity<Void> activateCategory( @PathVariable UUID categoryId ) {
        categoryService.activateCategory(categoryId);
        return ResponseEntity.noContent()
                .build();
    }

    @PatchMapping("/{categoryId}/deactivate")
    public ResponseEntity<Void> deactivateCategory( @PathVariable UUID categoryId ) {
        categoryService.deactivateCategory(categoryId);
        return ResponseEntity.noContent()
                .build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory( @PathVariable UUID categoryId ) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent()
                .build();
    }
}