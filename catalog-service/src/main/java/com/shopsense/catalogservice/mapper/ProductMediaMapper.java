package com.shopsense.catalogservice.mapper;

import com.shopsense.catalogservice.entity.ProductMedia;
import com.shopsense.catalogservice.request.ProductMediaRequest;
import com.shopsense.catalogservice.response.ProductMediaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMediaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductMedia toEntity( ProductMediaRequest request );

    @Mapping(target = "productId", source = "product.id")
    ProductMediaResponse toResponse( ProductMedia productMedia );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntity( ProductMediaRequest request, @MappingTarget ProductMedia productMedia );

}