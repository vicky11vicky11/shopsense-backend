package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    boolean existsByBrandNameIgnoreCase( String name );

    Page<Brand> findByActive( Boolean active, Pageable pageable );

    Optional<Brand> findByIdAndActive( UUID id, Boolean active );

}