package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.client.AddressServiceClient;
import com.shopsense.catalogservice.client.MediaServiceClient;
import com.shopsense.catalogservice.client.SellerServiceClient;
import com.shopsense.catalogservice.entity.Store;
import com.shopsense.catalogservice.enums.MediaType;
import com.shopsense.catalogservice.exception.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exception.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.StoreMapper;
import com.shopsense.catalogservice.repository.StoreRepository;
import com.shopsense.catalogservice.request.StoreRequest;
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

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final StoreMapper storeMapper;

    private final MediaServiceClient mediaServiceClient;

    private final SellerServiceClient sellerServiceClient;

    private final AddressServiceClient addressServiceClient;

    @Override
    @Transactional
    public StoreResponse createStore( StoreRequest request ) {
        boolean exists = storeRepository.existsBySellerIdAndAddressId(request.getSellerId(), request.getAddressId());
        if ( exists ) {
            throw new ResourceAlreadyExistsException("Store already exists for this seller and address");
        }
        Store store = storeMapper.toEntity(request);
        validateStoreImage(store.getStoreImageId());
        validateSellerId(store.getSellerId());
        validateAddress(store.getAddressId(),store.getSellerId());
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
    public PageResponse<StoreResponse> getStoresBySellerId( UUID sellerId, Pageable pageable ) {
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
        if ( !Objects.equals(store.getStoreImageId(), request.getStoreImageId()) ) {
            validateStoreImage(request.getStoreImageId());
            store.setStoreImageId(request.getStoreImageId());
        }
        Store savedStore = storeRepository.saveAndFlush(store);
        log.info("Store updated successfully with id: {}", storeId);
        return storeMapper.toResponse(savedStore);
    }

    @Override
    @Transactional
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
    @Transactional
    public void deleteStore( UUID storeId ) {
        Store store = findStoreById(storeId);
        storeRepository.delete(store);
        log.info("Store deleted successfully with id: {}", storeId);
    }

    private Store findStoreById( UUID storeId ) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
    }

    private void validateStoreImage( UUID storeImageId ) {
        if ( storeImageId == null ) {
            return;
        }
        boolean imageExist = mediaServiceClient.imageExist(storeImageId, MediaType.STORE);
        if ( !imageExist ) {
            throw new ResourceNotFoundException("Image not found with id: " + storeImageId);
        }
    }

    private void validateAddress( UUID addressId, UUID sellerId ) {
        boolean addressExists = addressServiceClient.isAddressExists(addressId, sellerId);
        if ( !addressExists ) {
            throw new ResourceNotFoundException("Address not found with id: " + addressId);
        }
    }

    private void validateSellerId( UUID sellerId ) {
        boolean sellerExists = sellerServiceClient.isSellerExists(sellerId);
        if ( !sellerExists ) {
            throw new ResourceNotFoundException("Seller not found with id: " + sellerId);
        }
    }
}
