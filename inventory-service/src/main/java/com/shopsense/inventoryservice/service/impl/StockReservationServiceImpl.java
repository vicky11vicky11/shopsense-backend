package com.shopsense.inventoryservice.service.impl;

import com.shopsense.inventoryservice.entity.StockMovement;
import com.shopsense.inventoryservice.entity.StockReservation;
import com.shopsense.inventoryservice.enums.ReservationStatus;
import com.shopsense.inventoryservice.enums.ReservationStatusResponse;
import com.shopsense.inventoryservice.enums.StockMovementType;
import com.shopsense.inventoryservice.exceptions.InsufficientStockException;
import com.shopsense.inventoryservice.exceptions.ReservationNotFoundException;
import com.shopsense.inventoryservice.repository.InventoryRepository;
import com.shopsense.inventoryservice.repository.StockMovementRepository;
import com.shopsense.inventoryservice.repository.StockReservationRepository;
import com.shopsense.inventoryservice.request.ReserveStockRequest;
import com.shopsense.inventoryservice.response.StockReservationResponse;
import com.shopsense.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationServiceImpl implements StockReservationService {

    private final InventoryRepository inventoryRepository;

    private final StockReservationRepository reservationRepository;

    private final StockMovementRepository movementRepository;

    @Override
    @Transactional
    public StockReservationResponse reserve( ReserveStockRequest request ) {
        UUID orderId = request.getOrderId();
        Set<UUID> productIds = new HashSet<>();
        for (ReserveStockRequest.ReservationItem item
                : request.getItems()) {
            if (!productIds.add(item.getProductId())) {
                throw new IllegalArgumentException(
                        "Duplicate product in reservation request: "
                                + item.getProductId()
                );
            }
        }
        if ( reservationRepository.existsByOrderId(orderId) ) {
            return buildExistingReservationResponse(orderId);
        }
        for ( ReserveStockRequest.ReservationItem item : request.getItems() ) {
            int updatedRows = inventoryRepository.reserveStock(item.getProductId(), item.getQuantity());
            if ( updatedRows == 0 ) {
                throw new InsufficientStockException(item.getProductId());
            }
        }
        List<StockReservation> reservations = new ArrayList<>();
        for ( ReserveStockRequest.ReservationItem item : request.getItems() ) {
            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.RESERVED)
                    .build();
            StockReservation saved = reservationRepository.save(reservation);
            reservations.add(saved);
            StockMovement movement = StockMovement.builder()
                    .productId(item.getProductId())
                    .type(StockMovementType.RESERVATION)
                    .quantity(item.getQuantity())
                    .referenceId(saved.getId())
                    .reason("Stock reserved for order: " + orderId)
                    .build();
            movementRepository.save(movement);
        }
        log.info("Stock reserved successfully: orderId={}, items={}", orderId, reservations.size());
        return buildResponse(orderId, ReservationStatusResponse.RESERVED, reservations);
    }

    @Override
    @Transactional
    public void release( UUID reservationId ) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if ( reservation.getStatus() != ReservationStatus.RESERVED ) {
            return;
        }
        int updatedRows = inventoryRepository.releaseStock(reservation.getProductId(), reservation.getQuantity());
        if ( updatedRows == 0 ) {
            throw new IllegalStateException("Unable to release reserved stock: " + reservationId);
        }
        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);
        StockMovement movement = StockMovement.builder()
                .productId(reservation.getProductId())
                .type(StockMovementType.RELEASE)
                .quantity(reservation.getQuantity())
                .referenceId(reservationId)
                .reason("Reservation released for order: " + reservation.getOrderId())
                .build();
        movementRepository.save(movement);
        log.info("Stock reservation released: reservationId={}", reservationId);
    }

    @Override
    @Transactional
    public void consume( UUID reservationId ) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if ( reservation.getStatus() != ReservationStatus.RESERVED ) {
            return;
        }
        int updatedRows = inventoryRepository.consumeStock(reservation.getProductId(), reservation.getQuantity());
        if ( updatedRows == 0 ) {
            throw new IllegalStateException("Unable to consume reserved stock: " + reservationId);
        }
        reservation.setStatus(ReservationStatus.CONSUMED);
        reservationRepository.save(reservation);
        StockMovement movement = StockMovement.builder()
                .productId(reservation.getProductId())
                .type(StockMovementType.STOCK_OUT)
                .quantity(reservation.getQuantity())
                .referenceId(reservationId)
                .reason("Stock consumed for order: " + reservation.getOrderId())
                .build();
        movementRepository.save(movement);
        log.info("Stock reservation consumed: reservationId={}", reservationId);
    }

    private StockReservationResponse buildExistingReservationResponse( UUID orderId ) {
        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        if ( reservations.isEmpty() ) {
            throw new IllegalStateException("Reservation state is inconsistent for order: " + orderId);
        }
        ReservationStatusResponse status = switch ( reservations.getFirst()
                .getStatus() ) {
            case RESERVED -> ReservationStatusResponse.RESERVED;
            case CONSUMED -> ReservationStatusResponse.CONSUMED;
            case RELEASED -> ReservationStatusResponse.RELEASED;
            case EXPIRED -> ReservationStatusResponse.RELEASED;
        };
        return buildResponse(orderId, status, reservations);
    }

    private StockReservationResponse buildResponse( UUID orderId, ReservationStatusResponse status, List<StockReservation> reservations ) {
        List<StockReservationResponse.ReservationItemResponse> items = reservations.stream()
                .map(reservation -> StockReservationResponse.ReservationItemResponse.builder()
                        .reservationId(reservation.getId())
                        .productId(reservation.getProductId())
                        .quantity(reservation.getQuantity())
                        .build())
                .toList();
        return StockReservationResponse.builder()
                .orderId(orderId)
                .status(status)
                .items(items)
                .createdAt(reservations.getFirst()
                        .getCreatedAt())
                .build();
    }
}