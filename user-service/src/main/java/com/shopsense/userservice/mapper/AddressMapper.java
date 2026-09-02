package com.shopsense.userservice.mapper;

import com.shopsense.userservice.entity.Address;
import com.shopsense.userservice.request.AddressRequest;
import com.shopsense.userservice.response.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    Address toEntity( AddressRequest addressRequest );

    AddressResponse toResponse( Address address );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            AddressRequest addressRequest,
            @MappingTarget Address address
    );
}
