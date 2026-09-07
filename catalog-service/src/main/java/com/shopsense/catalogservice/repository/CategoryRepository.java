package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByCategoryNameIgnoreCaseAndParentCategoryId( String categoryName, UUID parentCategoryId );

    Page<Category> findByActive( Boolean active, Pageable pageable );

    Page<Category> findByParentCategoryId( UUID parentCategoryId, Pageable pageable );

    Page<Category> findByParentCategoryIsNull( Pageable pageable );

    Optional<Category> findByIdAndActive( UUID id, Boolean active );

}