package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Brand;
import com.shopsense.catalogservice.exceptions.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.BrandMapper;
import com.shopsense.catalogservice.repository.BrandRepository;
import com.shopsense.catalogservice.request.BrandRequest;
import com.shopsense.catalogservice.response.BrandResponse;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.service.BrandService;
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
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    private final BrandMapper brandMapper;

    @Override
    @Transactional
    public BrandResponse createBrand( BrandRequest request ) {
        boolean exists = brandRepository.existsByBrandNameIgnoreCase(request.getBrandName());
        if ( exists ) {
            throw new ResourceAlreadyExistsException("Brand already exists with name: " + request.getBrandName());
        }
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.saveAndFlush(brand);
        log.info("Brand created successfully with id: {}", savedBrand.getId());
        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById( UUID brandId ) {
        Brand brand = findBrandById(brandId);
        log.info("Brand found successfully with id: {}", brandId);
        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> getAllBrands( Pageable pageable ) {
        Page<Brand> brands = brandRepository.findAll(pageable);
        log.info("Brands fetched successfully");
        return PageResponse.from(brands, brandMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> getActiveBrands( Pageable pageable ) {
        Page<Brand> brands = brandRepository.findByActive(true, pageable);
        log.info("Active brands fetched successfully");
        return PageResponse.from(brands, brandMapper::toResponse);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand( UUID brandId, BrandRequest request ) {
        Brand brand = findBrandById(brandId);
        if ( !brand.getBrandName()
                .equalsIgnoreCase(request.getBrandName()) ) {
            boolean exists = brandRepository.existsByBrandNameIgnoreCase(request.getBrandName());
            if ( exists ) {
                throw new ResourceAlreadyExistsException("Brand already exists with name: " + request.getBrandName());
            }
        }
        brandMapper.updateEntity(request, brand);
        Brand savedBrand = brandRepository.saveAndFlush(brand);
        log.info("Brand updated successfully with id: {}", brandId);
        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional
    public void activateBrand( UUID brandId ) {
        Brand brand = brandRepository.findByIdAndActive(brandId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with deactivated state"));
        brand.setActive(true);
        brandRepository.save(brand);
        log.info("Brand activated successfully with id: {}", brandId);
    }

    @Override
    @Transactional
    public void deactivateBrand( UUID brandId ) {
        Brand brand = brandRepository.findByIdAndActive(brandId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with activated state"));
        brand.setActive(false);
        brandRepository.save(brand);
        log.info("Brand deactivated successfully with id: {}", brandId);
    }

    @Override
    @Transactional
    public void deleteBrand( UUID brandId ) {
        Brand brand = findBrandById(brandId);
        brandRepository.delete(brand);
        log.info("Brand deleted successfully with id: {}", brandId);
    }

    private Brand findBrandById( UUID brandId ) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));
    }
}