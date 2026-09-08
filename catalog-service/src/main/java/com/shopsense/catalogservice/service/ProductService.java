package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.ProductRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct( ProductRequest request );

    ProductResponse getProductById( UUID productId );

    PageResponse<ProductResponse> getAllProducts( Pageable pageable );

    PageResponse<ProductResponse> getProductsByStoreId( UUID storeId, Pageable pageable );

    PageResponse<ProductResponse> getProductsByCategoryId( UUID categoryId, Pageable pageable );

    PageResponse<ProductResponse> getProductsByBrandId( UUID brandId, Pageable pageable );

    PageResponse<ProductResponse> getActiveProducts( Pageable pageable );

    ProductResponse updateProduct( UUID productId, ProductRequest request );

    void activateProduct( UUID productId );

    void deactivateProduct( UUID productId );

    void deleteProduct( UUID productId );
}
