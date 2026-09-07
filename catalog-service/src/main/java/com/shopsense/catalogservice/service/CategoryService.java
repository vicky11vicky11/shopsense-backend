package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.CategoryRequest;
import com.shopsense.catalogservice.response.CategoryResponse;
import com.shopsense.catalogservice.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {
    
    CategoryResponse createCategory( CategoryRequest request );

    CategoryResponse getCategoryById( UUID categoryId );

    PageResponse<CategoryResponse> getAllCategories( Pageable pageable );

    PageResponse<CategoryResponse> getActiveCategories( Pageable pageable );

    PageResponse<CategoryResponse> getRootCategories( Pageable pageable );

    PageResponse<CategoryResponse> getSubCategories( UUID parentCategoryId, Pageable pageable );

    CategoryResponse updateCategory( UUID categoryId, CategoryRequest request );

    void activateCategory( UUID categoryId );

    void deactivateCategory( UUID categoryId );

    void deleteCategory( UUID categoryId );

}