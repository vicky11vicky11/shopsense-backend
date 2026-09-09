package com.shopsense.catalogservice.mapper;

import com.shopsense.catalogservice.entity.ProductInformation;
import com.shopsense.catalogservice.request.ProductInformationRequest;
import com.shopsense.catalogservice.response.ProductInformationResponse;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductInformationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductInformation toEntity( ProductInformationRequest request );

    @Mapping(target = "productId", source = "product.id")
    ProductInformationResponse toResponse( ProductInformation productInformation );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity( ProductInformationRequest request, @MappingTarget ProductInformation productInformation );
}