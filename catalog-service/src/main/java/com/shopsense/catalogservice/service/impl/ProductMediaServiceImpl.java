package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Product;
import com.shopsense.catalogservice.entity.ProductMedia;
import com.shopsense.catalogservice.exceptions.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.ProductMediaMapper;
import com.shopsense.catalogservice.repository.ProductMediaRepository;
import com.shopsense.catalogservice.repository.ProductRepository;
import com.shopsense.catalogservice.request.ProductMediaRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductMediaResponse;
import com.shopsense.catalogservice.service.ProductMediaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductMediaServiceImpl implements ProductMediaService {

    private final ProductMediaRepository productMediaRepository;

    private final ProductRepository productRepository;

    private final ProductMediaMapper productMediaMapper;

    @Override
    public ProductMediaResponse create( UUID productId, ProductMediaRequest request ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if ( productMediaRepository.existsByProductIdAndMediaId(productId, request.getMediaId()) ) {
            throw new ResourceAlreadyExistsException("Media already exists for this product");
        }
        if ( request.isPrimaryImage() ) {
            unsetCurrentPrimaryImage(productId);
        }
        ProductMedia productMedia = productMediaMapper.toEntity(request);
        productMedia.setProduct(product);
        ProductMedia saved = productMediaRepository.save(productMedia);
        log.info("Product media created successfully: id={}, productId={}", saved.getId(), productId);
        return productMediaMapper.toResponse(saved);
    }

    @Override
    public List<ProductMediaResponse> createBulk( UUID productId, List<ProductMediaRequest> requests ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if ( requests.isEmpty() ) {
            throw new IllegalArgumentException("At least one product media is required");
        }
        List<String> mediaIds = requests.stream()
                .map(ProductMediaRequest::getMediaId)
                .toList();
        if ( mediaIds.size() != mediaIds.stream()
                .distinct()
                .count() ) {
            throw new IllegalArgumentException("Duplicate media IDs are not allowed");
        }
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
        List<ProductMedia> productMediaList = requests.stream()
                .map(request -> {
                    ProductMedia productMedia = productMediaMapper.toEntity(request);
                    productMedia.setProduct(product);
                    return productMedia;
                })
                .toList();
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
    public ProductMediaResponse update( UUID productId, UUID productMediaId, ProductMediaRequest request ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        boolean exists = productMediaRepository.existsByProductIdAndMediaId(productId, request.getMediaId());
        if ( !productMedia.getMediaId()
                .equals(request.getMediaId()) && exists ) {
            throw new ResourceAlreadyExistsException("Media already exists for this product");
        }
        if ( request.isPrimaryImage() && !productMedia.isPrimaryImage() ) {
            unsetCurrentPrimaryImage(productId);
        }
        productMediaMapper.updateEntity(request, productMedia);
        ProductMedia updated = productMediaRepository.save(productMedia);
        log.info("Product media updated successfully: productMediaId={}", productMediaId);
        return productMediaMapper.toResponse(updated);
    }

    @Override
    public void delete( UUID productId, UUID productMediaId ) {
        ProductMedia productMedia = getProductMedia(productId, productMediaId);
        productMediaRepository.delete(productMedia);
        log.info("Product media deleted successfully: productMediaId={}", productMediaId);
    }

    @Override
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
}