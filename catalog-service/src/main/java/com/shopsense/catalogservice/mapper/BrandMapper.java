package com.shopsense.catalogservice.mapper;

import com.shopsense.catalogservice.entity.Brand;
import com.shopsense.catalogservice.request.BrandRequest;
import com.shopsense.catalogservice.response.BrandResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Brand toEntity( BrandRequest request );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Brand updateEntity( BrandRequest request, @MappingTarget Brand brand );

    BrandResponse toResponse( Brand brand );
}
