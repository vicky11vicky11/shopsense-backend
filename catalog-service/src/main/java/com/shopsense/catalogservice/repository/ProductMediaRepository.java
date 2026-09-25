package com.shopsense.catalogservice.repository;

import com.shopsense.catalogservice.entity.ProductMedia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {

    boolean existsByProductIdAndMediaId( UUID productId, UUID mediaId );

    List<ProductMedia> findByProductIdAndMediaIdIn( UUID productId, Set<UUID> mediaIds );
    
    Optional<ProductMedia> findByProductIdAndPrimaryImageTrue( UUID productId );

    @Query("""
                SELECT COALESCE(MAX(pm.displayOrder), -1)
                FROM ProductMedia pm
                WHERE pm.product.id = :productId
            """)
    Integer findMaxDisplayOrderByProductId( UUID productId );

    @Modifying
    @Query("""
                update ProductMedia pm
                set pm.displayOrder = pm.displayOrder + 1
                where pm.product.id = :productId
                  and pm.displayOrder >= :newOrder
                  and pm.displayOrder < :oldOrder
            """)
    void incrementDisplayOrders( UUID productId, Integer newOrder, Integer oldOrder );

    @Modifying
    @Query("""
                update ProductMedia pm
                set pm.displayOrder = pm.displayOrder - 1
                where pm.product.id = :productId
                  and pm.displayOrder > :oldOrder
                  and pm.displayOrder <= :newOrder
            """)
    void decrementDisplayOrders( UUID productId, Integer oldOrder, Integer newOrder );

    Page<ProductMedia> findByProductIdOrderByDisplayOrderAsc( UUID productId, Pageable pageable );

    List<ProductMedia> findByProductIdOrderByDisplayOrderAsc(UUID productId);


    Optional<ProductMedia> findFirstByProductIdAndIdNotOrderByDisplayOrderAsc( UUID productId, UUID id );
}