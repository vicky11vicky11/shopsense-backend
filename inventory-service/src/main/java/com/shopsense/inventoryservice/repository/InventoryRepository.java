package com.shopsense.inventoryservice.repository;

import com.shopsense.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId( UUID productId );

    boolean existsByProductId( UUID productId );

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.reservedQuantity =
                    i.reservedQuantity + :quantity
                WHERE i.productId = :productId
                  AND i.quantity - i.reservedQuantity >= :quantity
            """)
    int reserveStock( @Param("productId") UUID productId, @Param("quantity") Integer quantity );

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.reservedQuantity =
                    i.reservedQuantity - :quantity
                WHERE i.productId = :productId
                  AND i.reservedQuantity >= :quantity
            """)
    int releaseStock( @Param("productId") UUID productId, @Param("quantity") Integer quantity );

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.quantity = i.quantity - :quantity,
                    i.reservedQuantity =
                        i.reservedQuantity - :quantity
                WHERE i.productId = :productId
                  AND i.reservedQuantity >= :quantity
            """)
    int consumeStock( @Param("productId") UUID productId, @Param("quantity") Integer quantity );

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.quantity = i.quantity + :quantity
                WHERE i.productId = :productId
                  AND i.quantity + :quantity >= i.reservedQuantity
            """)
    int adjustStock( @Param("productId") UUID productId, @Param("quantity") Integer quantity );

    List<Inventory> findByProductIdIn( List<UUID> productIds);
}