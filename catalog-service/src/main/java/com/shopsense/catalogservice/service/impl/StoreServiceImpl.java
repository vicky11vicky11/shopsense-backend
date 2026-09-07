package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Store;
import com.shopsense.catalogservice.exceptions.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.StoreMapper;
import com.shopsense.catalogservice.repository.StoreRepository;
import com.shopsense.catalogservice.request.CreateStoreRequest;
import com.shopsense.catalogservice.request.UpdateStoreRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.StoreResponse;
import com.shopsense.catalogservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final StoreMapper storeMapper;

    @Override
    @Transactional
    public StoreResponse createStore( CreateStoreRequest request ) {
        boolean exists = storeRepository.existsBySellerIdAndAddressId(request.getSellerId(), request.getAddressId());
        if ( exists ) {
            throw new ResourceAlreadyExistsException("Store already exists for this seller and address");
        }
        Store store = storeMapper.toEntity(request);
        store.setActive(true);
        Store savedStore = storeRepository.saveAndFlush(store);
        log.info("Store created successfully with id: {}", savedStore.getId());
        return storeMapper.toResponse(savedStore);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById( UUID storeId ) {
        Store store = findStoreById(storeId);
        log.info("Store found successfully with id: {}", storeId);
        return storeMapper.toResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getStoresBySellerId( String sellerId, Pageable pageable ) {
        Page<Store> stores = storeRepository.findBySellerId(sellerId, pageable);
        log.info("Stores found successfully with seller id: {}", sellerId);
        return PageResponse.from(stores, storeMapper::toResponse);
    }

    @Override
    @Transactional
    public StoreResponse updateStore( UUID storeId, UpdateStoreRequest request ) {
        Store store = findStoreById(storeId);
        if ( request.getStoreName() != null ) {
            store.setStoreName(request.getStoreName());
        }
        if ( request.getStoreImageId() != null ) {
            store.setStoreImageId(request.getStoreImageId());
        }
        Store savedStore = storeRepository.saveAndFlush(store);
        log.info("Store updated successfully with id: {}", storeId);
        return storeMapper.toResponse(savedStore);
    }

    @Override
    public void activateStore( UUID storeId ) {
        Store store = storeRepository.findByIdAndActive(storeId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with deactivated state"));
        store.setActive(true);
        storeRepository.save(store);
        log.info("Store activated successfully with id: {}", storeId);
    }

    @Override
    @Transactional
    public void deactivateStore( UUID storeId ) {
        Store store = storeRepository.findByIdAndActive(storeId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with activated state"));
        store.setActive(false);
        storeRepository.save(store);
        log.info("Store deactivated successfully with id: {}", storeId);
    }

    @Override
    public void deleteStore( UUID storeId ) {
        Store store = findStoreById(storeId);
        storeRepository.delete(store);
        log.info("Store deleted successfully with id: {}", storeId);
    }

    private Store findStoreById( UUID storeId ) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
    }

}
