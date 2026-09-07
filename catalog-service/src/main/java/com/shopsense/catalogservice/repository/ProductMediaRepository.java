package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {
}