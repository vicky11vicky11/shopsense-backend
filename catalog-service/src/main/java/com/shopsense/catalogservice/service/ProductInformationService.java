package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.ProductInformationRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductInformationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductInformationService {

    ProductInformationResponse create( UUID productId, ProductInformationRequest request );

    PageResponse<ProductInformationResponse> getByProductId( UUID productId, Pageable pageable );

    ProductInformationResponse getById( UUID productId, UUID productInformationId );

    ProductInformationResponse update( UUID productId, UUID productInformationId, ProductInformationRequest request );

    void delete( UUID productId, UUID productInformationId );

}