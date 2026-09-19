package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.client.MediaServiceClient;
import com.shopsense.catalogservice.entity.Product;
import com.shopsense.catalogservice.entity.ProductMedia;
import com.shopsense.catalogservice.enums.MediaType;
import com.shopsense.catalogservice.exceptions.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.ProductMediaMapper;
import com.shopsense.catalogservice.repository.ProductMediaRepository;
import com.shopsense.catalogservice.repository.ProductRepository;
import com.shopsense.catalogservice.request.ProductMediaRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductMediaResponse;
import com.shopsense.catalogservice.service.ProductMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductMediaServiceImpl implements ProductMediaService {

    private final ProductMediaRepository productMediaRepository;

    private final ProductRepository productRepository;

    private final ProductMediaMapper productMediaMapper;

    private final MediaServiceClient mediaServiceClient;

    @Override
    @Transactional
    public ProductMediaResponse create( UUID productId, ProductMediaRequest request ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if ( productMediaRepository.existsByProductIdAndMediaId(productId, request.getMediaId()) ) {
            throw new ResourceAlreadyExistsException("Media already exists for this product");
        }
        validateProductImage(request.getMediaId());
        if ( request.isPrimaryImage() ) {
            unsetCurrentPrimaryImage(productId);
        }
        int nextDisplayOrder = productMediaRepository.findMaxDisplayOrderByProductId(productId) + 1;
        ProductMedia productMedia = productMediaMapper.toEntity(request);
        productMedia.setProduct(product);
        productMedia.setDisplayOrder(nextDisplayOrder);
        ProductMedia saved = productMediaRepository.save(productMedia);
        log.info("Product media created successfully: id={}, productId={}, displayOrder={}", saved.getId(), productId, saved.getDisplayOrder());
        return productMediaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public List<ProductMediaResponse> createBulk( UUID productId, List<ProductMediaRequest> requests ) {
        if ( requests == null || requests.isEmpty() ) {
            throw new IllegalArgumentException("At least one product media is required");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        List<String> mediaIds = requests.stream()
                .map(ProductMediaRequest::getMediaId)
                .toList();
        if ( mediaIds.size() != mediaIds.stream()
                .distinct()
                .count() ) {
            throw new IllegalArgumentException("Duplicate media IDs are not allowed");
        }
        mediaIds.forEach(this::validateProductImage);
        List<ProductMedia> existingMedia = productMediaRepository.findByProductIdAndMediaIdIn(productId, new HashSet<>(mediaIds));
        if ( !existingMedia.isEmpty() ) {
            String existingMediaId = existingMedia.getFirst()
                    .getMediaId();
            throw new ResourceAlreadyExistsException("Media already exists for this product: " + existingMediaId);
        }
        long primaryCount = requests.stream()
                .filter(ProductMediaRequest::isPrimaryImage)
                .count();
        if ( primaryCount > 1 ) {
            throw new IllegalArgumentException("Only one primary image is allowed");
        }
        if ( primaryCount == 1 ) {
            unsetCurrentPrimaryImage(productId);
        }
        int nextDisplayOrder = productMediaRepository.findMaxDisplayOrderByProductId(productId) + 1;
        List<ProductMedia> productMediaList = new java.util.ArrayList<>();
        for ( ProductMediaRequest request : requests ) {
            ProductMedia productMedia = productMediaMapper.toEntity(request);
            productMedia.setProduct(product);
            productMedia.setDisplayOrder(nextDisplayOrder++);
            productMediaList.add(productMedia);
        }
        List<ProductMedia> saved = productMediaRepository.saveAll(productMediaList);
        log.info("Product media created successfully in bulk: productId={}, count={}", productId, saved.size());
        return saved.stream()
                .map(productMediaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductMediaResponse> getByProductId( UUID productId, Pageable pageable ) {
        boolean exists = productRepository.existsById(productId);
        if ( !exists ) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        Page<ProductMedia> productMediaResponse = productMediaRepository.findByProductIdOrderByDisplayOrderAsc(productId, pageable);
        return PageResponse.from(productMediaResponse, productMediaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductMediaResponse getById( UUID productId, UUID productMediaId ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        return productMediaMapper.toResponse(productMedia);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductMediaResponse getProductPrimaryImage( UUID productId ) {
        ProductMedia productMedia = productMediaRepository.findByProductIdAndPrimaryImageTrue(productId)
                .orElse(null);
        log.info("Product media primary image found: {}", productMedia);
        return productMediaMapper.toResponse(productMedia);
    }

    @Override
    @Transactional
    public ProductMediaResponse update( UUID productId, UUID productMediaId, ProductMediaRequest request ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        if ( !Objects.equals(productMedia.getMediaId(), request.getMediaId()) ) {
            boolean exists = productMediaRepository.existsByProductIdAndMediaId(productId, request.getMediaId());
            if ( exists ) {
                throw new ResourceAlreadyExistsException("Media already exists for this product");
            }
            validateProductImage(request.getMediaId());
        }
        if ( !productMedia.getDisplayOrder()
                .equals(request.getDisplayOrder()) ) {
            updateDisplayOrder(productId, productMedia, request.getDisplayOrder());
        }
        if ( request.isPrimaryImage() && !productMedia.isPrimaryImage() ) {
            unsetCurrentPrimaryImage(productId);
        }
        productMediaMapper.updateEntity(request, productMedia);
        ProductMedia updated = productMediaRepository.save(productMedia);
        log.info("Product media updated successfully: productMediaId={}, productId={}", productMediaId, productId);
        return productMediaMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete( UUID productId, UUID productMediaId ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        if ( productMedia.isPrimaryImage() ) {
            productMediaRepository.findFirstByProductIdAndIdNotOrderByDisplayOrderAsc(productId, productMediaId)
                    .ifPresent(otherMedia -> {
                        otherMedia.setPrimaryImage(true);
                        log.info("Primary image reassigned: productId={}, newPrimaryMediaId={}", productId, otherMedia.getId());
                    });
        }
        productMediaRepository.delete(productMedia);
        log.info("Product media deleted successfully: productMediaId={}", productMediaId);
    }

    @Override
    @Transactional
    public ProductMediaResponse setPrimaryImage( UUID productId, UUID productMediaId ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        if ( !productMedia.isPrimaryImage() ) {
            unsetCurrentPrimaryImage(productId);
            productMedia.setPrimaryImage(true);
        }
        ProductMedia saved = productMediaRepository.save(productMedia);
        log.info("Primary image updated: productId={}, productMediaId={}", productId, productMediaId);
        return productMediaMapper.toResponse(saved);
    }

    private ProductMedia getProductMedia( UUID productId, UUID productMediaId ) {
        ProductMedia productMedia = productMediaRepository.findById(productMediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Product media not found: " + productMediaId));
        if ( !productMedia.getProduct()
                .getId()
                .equals(productId) ) {
            throw new ResourceNotFoundException("Product media not found for product: " + productId);
        }
        return productMedia;
    }

    private void unsetCurrentPrimaryImage( UUID productId ) {
        productMediaRepository.findByProductIdAndPrimaryImageTrue(productId)
                .ifPresent(currentPrimary -> {
                    currentPrimary.setPrimaryImage(false);
                });
    }

    private void updateDisplayOrder( UUID productId, ProductMedia productMedia, Integer newOrder ) {
        Integer oldOrder = productMedia.getDisplayOrder();
        if ( newOrder == null ) {
            throw new IllegalArgumentException("Display order cannot be null");
        }
        if ( newOrder < 0 ) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }
        productMedia.setDisplayOrder(Integer.MIN_VALUE);
        productMediaRepository.saveAndFlush(productMedia);
        if ( newOrder < oldOrder ) {
            productMediaRepository.incrementDisplayOrders(productId, newOrder, oldOrder);
        } else {
            productMediaRepository.decrementDisplayOrders(productId, oldOrder, newOrder);
        }
        productMedia.setDisplayOrder(newOrder);
    }

    private void validateProductImage( String productImageId ) {
        boolean imageExist = mediaServiceClient.imageExist(productImageId, MediaType.PRODUCT);
        if ( !imageExist ) {
            throw new ResourceNotFoundException("Product image not found: " + productImageId);
        }
    }
}