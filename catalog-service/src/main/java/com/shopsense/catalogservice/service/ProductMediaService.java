package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.ProductMediaRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductMediaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductMediaService {

    ProductMediaResponse create( UUID productId, ProductMediaRequest request );

    List<ProductMediaResponse> createBulk( UUID productId, @Valid List<ProductMediaRequest> requests );

    ProductMediaResponse getById( UUID productId, UUID productMediaId );

    PageResponse<ProductMediaResponse> getByProductId( UUID productId, Pageable pageable );

    ProductMediaResponse update( UUID productId, UUID productMediaId, ProductMediaRequest request );

    void delete( UUID productId, UUID productMediaId );

    ProductMediaResponse setPrimaryImage( UUID productId, UUID productMediaId );

}