package com.shopsense.inventoryservice.repository;

import com.shopsense.inventoryservice.entity.StockReservation;
import com.shopsense.inventoryservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findByOrderId( UUID orderId );

    boolean existsByOrderId( UUID orderId );

    List<StockReservation> findByOrderIdAndStatus( UUID orderId, ReservationStatus status );
}