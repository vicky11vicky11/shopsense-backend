package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Product;
import com.shopsense.catalogservice.entity.ProductInformation;
import com.shopsense.catalogservice.mapper.ProductInformationMapper;
import com.shopsense.catalogservice.repository.ProductInformationRepository;
import com.shopsense.catalogservice.repository.ProductRepository;
import com.shopsense.catalogservice.request.ProductInformationRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductInformationResponse;
import com.shopsense.catalogservice.service.ProductInformationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductInformationServiceImpl implements ProductInformationService {

    private final ProductInformationRepository productInformationRepository;

    private final ProductRepository productRepository;

    private final ProductInformationMapper productInformationMapper;

    @Override
    public ProductInformationResponse create( UUID productId, ProductInformationRequest request ) {
        log.info("Creating product information: productId={}, attributeName={}", productId, request.getAttributeName());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found: productId={}", productId);
                    return new EntityNotFoundException("Product not found: " + productId);
                });
        if ( productInformationRepository.existsByProductIdAndAttributeName(productId, request.getAttributeName()) ) {
            log.warn("Attribute already exists for product: productId={}, attributeName={}", productId, request.getAttributeName());
            throw new IllegalArgumentException("Attribute already exists for this product");
        }
        ProductInformation productInformation = productInformationMapper.toEntity(request);
        productInformation.setProduct(product);
        ProductInformation saved = productInformationRepository.save(productInformation);
        log.info("Product information created successfully: id={}, productId={}", saved.getId(), productId);
        return productInformationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductInformationResponse> getByProductId( UUID productId, Pageable pageable ) {
        log.debug("Fetching product information: productId={}, page={}, size={}", productId, pageable.getPageNumber(), pageable.getPageSize());
        if ( !productRepository.existsById(productId) ) {
            log.warn("Product not found: productId={}", productId);
            throw new EntityNotFoundException("Product not found: " + productId);
        }
        return PageResponse.from(productInformationRepository.findByProductIdOrderByAttributeNameAsc(productId, pageable), productInformationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInformationResponse getById( UUID productId, UUID productInformationId ) {
        log.debug("Fetching product information: productId={}, productInformationId={}", productId, productInformationId);
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        return productInformationMapper.toResponse(productInformation);
    }

    @Override
    public ProductInformationResponse update( UUID productId, UUID productInformationId, ProductInformationRequest request ) {
        log.info("Updating product information: productId={}, productInformationId={}", productId, productInformationId);
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        if ( !productInformation.getAttributeName()
                .equals(request.getAttributeName()) && productInformationRepository.existsByProductIdAndAttributeName(productId, request.getAttributeName()) ) {
            log.warn("Attribute already exists for product: productId={}, attributeName={}", productId, request.getAttributeName());
            throw new IllegalArgumentException("Attribute already exists for this product");
        }
        productInformationMapper.updateEntity(request, productInformation);
        ProductInformation updated = productInformationRepository.save(productInformation);
        log.info("Product information updated successfully: productInformationId={}", productInformationId);
        return productInformationMapper.toResponse(updated);
    }

    @Override
    public void delete( UUID productId, UUID productInformationId ) {
        log.info("Deleting product information: productId={}, productInformationId={}", productId, productInformationId);
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        productInformationRepository.delete(productInformation);
        log.info("Product information deleted successfully: productInformationId={}", productInformationId);
    }

    private ProductInformation getProductInformation( UUID productId, UUID productInformationId ) {
        ProductInformation productInformation = productInformationRepository.findById(productInformationId)
                .orElseThrow(() -> {
                    log.warn("Product information not found: productInformationId={}", productInformationId);
                    return new EntityNotFoundException("Product information not found: " + productInformationId);
                });
        if ( !productInformation.getProduct()
                .getId()
                .equals(productId) ) {
            log.warn("Product information does not belong to product: productId={}, productInformationId={}", productId, productInformationId);
            throw new EntityNotFoundException("Product information not found for product: " + productId);
        }
        return productInformation;
    }
}