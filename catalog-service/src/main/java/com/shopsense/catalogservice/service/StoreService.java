package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.StoreRequest;
import com.shopsense.catalogservice.request.UpdateStoreRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.StoreResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreService {

    StoreResponse createStore( StoreRequest request );

    StoreResponse getStoreById( UUID storeId );

    PageResponse<StoreResponse> getStoresBySellerId( String sellerId, Pageable pageable );

    StoreResponse updateStore( UUID storeId, UpdateStoreRequest request );

    void activateStore( UUID storeId );

    void deactivateStore( UUID storeId );

    void deleteStore( UUID storeId );
}