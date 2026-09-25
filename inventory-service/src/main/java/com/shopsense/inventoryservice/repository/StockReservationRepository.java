package com.shopsense.inventoryservice.repository;

import com.shopsense.inventoryservice.entity.StockReservation;
import com.shopsense.inventoryservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findByOrderId( UUID orderId );

    boolean existsByOrderId( UUID orderId );

    List<StockReservation> findByOrderIdAndStatus( UUID orderId, ReservationStatus status );

    @Query("""
                SELECT r
                FROM StockReservation r
                WHERE r.status = ReservationStatus.RESERVED
                  AND r.expiresAt <= :now
            """)
    List<StockReservation> findExpiredReservations( Instant now );

    List<StockReservation> findAllByOrderId( UUID orderId );
}