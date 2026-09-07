package com.shopsense.catalogservice.mapper;

import com.shopsense.catalogservice.entity.Store;
import com.shopsense.catalogservice.request.CreateStoreRequest;
import com.shopsense.catalogservice.response.StoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StoreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Store toEntity( CreateStoreRequest request );

    StoreResponse toResponse( Store store );

}
