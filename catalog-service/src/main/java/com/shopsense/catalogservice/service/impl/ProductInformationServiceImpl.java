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
import jakarta.validation.Valid;
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
public class ProductInformationServiceImpl implements ProductInformationService {

    private final ProductInformationRepository productInformationRepository;

    private final ProductRepository productRepository;

    private final ProductInformationMapper productInformationMapper;

    @Override
    @Transactional
    public ProductInformationResponse create( UUID productId, ProductInformationRequest request ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        if ( productInformationRepository.existsByProductIdAndAttributeName(productId, request.getAttributeName()) ) {
            throw new IllegalArgumentException("Attribute already exists for this product");
        }
        ProductInformation productInformation = productInformationMapper.toEntity(request);
        productInformation.setProduct(product);
        ProductInformation saved = productInformationRepository.saveAndFlush(productInformation);
        log.info("Product information created successfully: id={}, productId={}", saved.getId(), productId);
        return productInformationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public List<ProductInformationResponse> bulkCreate( UUID productId, List<ProductInformationRequest> requests ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        if ( requests == null || requests.isEmpty() ) {
            throw new IllegalArgumentException("Product information list cannot be empty");
        }
        Set<String> existingAttributes = productInformationRepository.findAttributeNamesByProductId(productId);
        Set<String> requestedAttributes = new HashSet<>();
        for ( ProductInformationRequest request : requests ) {
            String attributeName = request.getAttributeName();
            if ( !requestedAttributes.add(attributeName) ) {
                throw new IllegalArgumentException("Duplicate attribute in request: " + attributeName);
            }
            if ( existingAttributes.contains(attributeName) ) {
                throw new IllegalArgumentException("Attribute already exists for this product: " + attributeName);
            }
        }
        List<ProductInformation> productInformations = requests.stream()
                .map(request -> {
                    ProductInformation productInformation = productInformationMapper.toEntity(request);
                    productInformation.setProduct(product);
                    return productInformation;
                })
                .toList();
        List<ProductInformation> saved = productInformationRepository.saveAllAndFlush(productInformations);
        log.info("Product informations bulk created successfully: productId={}, count={}", productId, saved.size());
        return saved.stream()
                .map(productInformationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductInformationResponse> getByProductId( UUID productId, Pageable pageable ) {
        if ( !productRepository.existsById(productId) ) {
            throw new EntityNotFoundException("Product not found: " + productId);
        }
        Page<ProductInformation> productInformations = productInformationRepository.findByProductIdOrderByAttributeNameAsc(productId, pageable);
        log.info("Product informations found successfully");
        return PageResponse.from(productInformations, productInformationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInformationResponse getById( UUID productId, UUID productInformationId ) {
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        log.info("Product information get successfully");
        return productInformationMapper.toResponse(productInformation);
    }

    @Override
    @Transactional
    public ProductInformationResponse update( UUID productId, UUID productInformationId, ProductInformationRequest request ) {
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        if ( !productInformation.getAttributeName()
                .equals(request.getAttributeName()) && productInformationRepository.existsByProductIdAndAttributeName(productId, request.getAttributeName()) ) {
            throw new IllegalArgumentException("Attribute already exists for this product");
        }
        productInformationMapper.updateEntity(request, productInformation);
        ProductInformation updated = productInformationRepository.saveAndFlush(productInformation);
        log.info("Product information updated successfully: productInformationId={}", productInformationId);
        return productInformationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete( UUID productId, UUID productInformationId ) {
        ProductInformation productInformation = getProductInformation(productId, productInformationId);
        productInformationRepository.delete(productInformation);
        log.info("Product information deleted successfully: productInformationId={}", productInformationId);
    }

    private ProductInformation getProductInformation( UUID productId, UUID productInformationId ) {
        return productInformationRepository.findByIdAndProductId(productInformationId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Product information not found: " + productInformationId));
    }
}