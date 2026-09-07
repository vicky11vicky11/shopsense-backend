package com.shopsense.catalogservice.service;

import com.shopsense.catalogservice.request.BrandRequest;
import com.shopsense.catalogservice.response.BrandResponse;
import com.shopsense.catalogservice.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BrandService {

    BrandResponse createBrand( BrandRequest request );

    BrandResponse getBrandById( UUID brandId );

    PageResponse<BrandResponse> getAllBrands( Pageable pageable );

    PageResponse<BrandResponse> getActiveBrands( Pageable pageable );

    BrandResponse updateBrand( UUID brandId, BrandRequest request );

    void activateBrand( UUID brandId );

    void deactivateBrand( UUID brandId );

    void deleteBrand( UUID brandId );
}
