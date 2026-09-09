package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Brand;
import com.shopsense.catalogservice.entity.Category;
import com.shopsense.catalogservice.entity.Product;
import com.shopsense.catalogservice.entity.Store;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.ProductMapper;
import com.shopsense.catalogservice.repository.BrandRepository;
import com.shopsense.catalogservice.repository.CategoryRepository;
import com.shopsense.catalogservice.repository.ProductRepository;
import com.shopsense.catalogservice.repository.StoreRepository;
import com.shopsense.catalogservice.request.ProductRequest;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.response.ProductResponse;
import com.shopsense.catalogservice.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;

    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct( ProductRequest request ) {
        Store store = findActiveStoreById(request.getStoreId());
        Category category = findActiveCategoryById(request.getCategoryId());
        Brand brand = null;
        if ( request.getBrandId() != null ) {
            brand = findActiveBrandById(request.getBrandId());
        }
        Product product = productMapper.toEntity(request);
        product.setStore(store);
        product.setCategory(category);
        product.setBrand(brand);
        Product savedProduct = productRepository.saveAndFlush(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById( UUID productId ) {
        Product product = findProductById(productId);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts( Pageable pageable ) {
        Page<Product> products = productRepository.findAll(pageable);
        return PageResponse.from(products, productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByStoreId( UUID storeId, Pageable pageable ) {
        Page<Product> products = productRepository.findByStoreId(storeId, pageable);
        return PageResponse.from(products, productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategoryId( UUID categoryId, Pageable pageable ) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return PageResponse.from(products, productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByBrandId( UUID brandId, Pageable pageable ) {
        Page<Product> products = productRepository.findByBrandId(brandId, pageable);
        return PageResponse.from(products, productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getActiveProducts( Pageable pageable ) {
        Page<Product> products = productRepository.findByActive(true, pageable);
        return PageResponse.from(products, productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct( UUID productId, ProductRequest request ) {
        Product product = findProductById(productId);
        Store store = findActiveStoreById(request.getStoreId());
        Category category = findActiveCategoryById(request.getCategoryId());
        Brand brand = null;
        if ( request.getBrandId() != null ) {
            brand = brandRepository.findByIdAndActive(request.getBrandId(), true)
                    .orElseThrow(() -> new ResourceNotFoundException("Active brand not found with id: " + request.getBrandId()));
        }
        productMapper.updateEntity(request, product);
        product.setStore(store);
        product.setCategory(category);
        product.setBrand(brand);
        Product savedProduct = productRepository.saveAndFlush(product);
        log.info("Product updated successfully with id: {}", productId);
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public void activateProduct( UUID productId ) {
        Product product = findProductByIdAndStatus(productId, false);
        product.setActive(true);
        productRepository.save(product);
        log.info("Product activated successfully with id: {}", productId);
    }

    @Override
    @Transactional
    public void deactivateProduct( UUID productId ) {
        Product product = findProductByIdAndStatus(productId, true);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deactivated successfully with id: {}", productId);
    }

    @Override
    @Transactional
    public void deleteProduct( UUID productId ) {
        Product product = findProductById(productId);
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", productId);
    }

    private Product findProductById( UUID productId ) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Product findProductByIdAndStatus( UUID productId, boolean status ) {
        return productRepository.findByIdAndActive(productId, status)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId + " and with active status " + status));
    }

    private Store findActiveStoreById( UUID storeId ) {
        return storeRepository.findByIdAndActive(storeId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Active store not found with id: " + storeId));
    }

    private Category findActiveCategoryById( UUID categoryId ) {
        return categoryRepository.findByIdAndActive(categoryId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found with id: " + categoryId));
    }

    private Brand findActiveBrandById( UUID brandId ) {
        return brandRepository.findByIdAndActive(brandId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Active brand not found with id: " + brandId));
    }
}