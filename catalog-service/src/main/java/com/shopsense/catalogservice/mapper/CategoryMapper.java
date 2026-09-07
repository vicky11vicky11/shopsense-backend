package com.shopsense.catalogservice.mapper;

import com.shopsense.catalogservice.entity.Category;
import com.shopsense.catalogservice.request.CategoryRequest;
import com.shopsense.catalogservice.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity( CategoryRequest request );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category updateEntity( CategoryRequest request, @MappingTarget Category category );

    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    CategoryResponse toResponse( Category category );
}