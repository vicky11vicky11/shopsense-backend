package com.shopsense.userservice.mapper;

import com.shopsense.userservice.entity.User;
import com.shopsense.userservice.request.UserRequest;
import com.shopsense.userservice.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "role",ignore = true)
    @Mapping(target = "status",ignore = true)
    @Mapping(target = "emailVerified",ignore = true)
    @Mapping(target = "phoneVerified",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    @Mapping(target = "version",ignore = true)
    User toEntity( UserRequest userRequest);

    UserResponse toResponse( User user);

}
