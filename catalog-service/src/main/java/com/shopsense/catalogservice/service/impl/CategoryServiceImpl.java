package com.shopsense.catalogservice.service.impl;

import com.shopsense.catalogservice.entity.Category;
import com.shopsense.catalogservice.exceptions.ResourceAlreadyExistsException;
import com.shopsense.catalogservice.exceptions.ResourceNotFoundException;
import com.shopsense.catalogservice.mapper.CategoryMapper;
import com.shopsense.catalogservice.repository.CategoryRepository;
import com.shopsense.catalogservice.request.CategoryRequest;
import com.shopsense.catalogservice.response.CategoryResponse;
import com.shopsense.catalogservice.response.PageResponse;
import com.shopsense.catalogservice.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory( CategoryRequest request ) {
        boolean exists = categoryRepository.existsByCategoryNameIgnoreCaseAndParentCategoryId(request.getCategoryName(), request.getParentCategoryId());
        if ( exists ) {
            throw new ResourceAlreadyExistsException("Category already exists with name: " + request.getCategoryName());
        }
        Category parentCategory = null;
        if ( request.getParentCategoryId() != null ) {
            parentCategory = findCategoryById(request.getParentCategoryId());
        }
        Category category = categoryMapper.toEntity(request);
        category.setParentCategory(parentCategory);
        category.setActive(true);
        Category savedCategory = categoryRepository.saveAndFlush(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById( UUID categoryId ) {
        Category category = findCategoryById(categoryId);
        log.info("Category found successfully with id: {}", categoryId);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAllCategories( Pageable pageable ) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return PageResponse.from(categories, categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getActiveCategories( Pageable pageable ) {
        Page<Category> categories = categoryRepository.findByActive(true, pageable);
        return PageResponse.from(categories, categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getRootCategories( Pageable pageable ) {
        Page<Category> categories = categoryRepository.findByParentCategoryIsNull(pageable);
        return PageResponse.from(categories, categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getSubCategories( UUID parentCategoryId, Pageable pageable ) {
        findCategoryById(parentCategoryId);
        Page<Category> categories = categoryRepository.findByParentCategoryId(parentCategoryId, pageable);
        return PageResponse.from(categories, categoryMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory( UUID categoryId, CategoryRequest request ) {
        Category category = findCategoryById(categoryId);
        UUID currentParentId = category.getParentCategory() != null ? category.getParentCategory()
                .getId() : null;
        boolean nameChanged = !category.getCategoryName()
                .equalsIgnoreCase(request.getCategoryName());
        boolean parentChanged = !java.util.Objects.equals(currentParentId, request.getParentCategoryId());
        if ( nameChanged || parentChanged ) {
            boolean exists = categoryRepository.existsByCategoryNameIgnoreCaseAndParentCategoryId(request.getCategoryName(), request.getParentCategoryId());
            if ( exists ) {
                throw new ResourceAlreadyExistsException("Category already exists with name: " + request.getCategoryName());
            }
        }
        if ( categoryId.equals(request.getParentCategoryId()) ) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        Category parentCategory = null;
        if ( request.getParentCategoryId() != null ) {
            parentCategory = findCategoryById(request.getParentCategoryId());
        }
        categoryMapper.updateEntity(request, category);
        category.setParentCategory(parentCategory);
        Category savedCategory = categoryRepository.saveAndFlush(category);
        log.info("Category updated successfully with id: {}", categoryId);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public void activateCategory( UUID categoryId ) {
        Category category = categoryRepository.findByIdAndActive(categoryId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with deactivated state"));
        category.setActive(true);
        categoryRepository.save(category);
        log.info("Category activated successfully with id: {}", categoryId);
    }

    @Override
    @Transactional
    public void deactivateCategory( UUID categoryId ) {
        Category category = categoryRepository.findByIdAndActive(categoryId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with activated state"));
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Category deactivated successfully with id: {}", categoryId);
    }

    @Override
    @Transactional
    public void deleteCategory( UUID categoryId ) {
        Category category = findCategoryById(categoryId);
        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", categoryId);
    }

    private Category findCategoryById( UUID categoryId ) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }
}
