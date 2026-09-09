package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.ProductMedia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {

    Page<ProductMedia> findByProductIdOrderByDisplayOrderAsc( UUID productId, Pageable pageable );

    boolean existsByProductIdAndMediaId( UUID productId, String mediaId );

    Optional<ProductMedia> findByProductIdAndPrimaryImageTrue( UUID productId );

    List<ProductMedia> findByProductIdAndMediaIdIn( UUID productId, Set<String> mediaIds );
}