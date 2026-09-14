package com.shopsense.inventoryservice.service.impl;

import com.shopsense.inventoryservice.entity.StockMovement;
import com.shopsense.inventoryservice.entity.StockReservation;
import com.shopsense.inventoryservice.enums.ReservationStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationServiceImpl implements StockReservationService {

    @Value("${reservation.expiry}")
    private long RESERVATION_DURATION_MINUTES;

    private final InventoryRepository inventoryRepository;

    private final StockReservationRepository reservationRepository;

    private final StockMovementRepository movementRepository;

    @Override
    @Transactional
    public StockReservationResponse reserve( ReserveStockRequest request ) {
        UUID orderId = request.getOrderId();
        validateDuplicateProducts(request);
        if ( reservationRepository.existsByOrderId(orderId) ) {
            return getByOrderId(orderId);
        }
        Instant expiresAt = Instant.now()
                .plus(RESERVATION_DURATION_MINUTES, ChronoUnit.MINUTES);
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
                    .expiresAt(expiresAt)
                    .build();
            StockReservation saved = reservationRepository.saveAndFlush(reservation);
            reservations.add(saved);
            StockMovement movement = StockMovement.builder()
                    .productId(item.getProductId())
                    .type(StockMovementType.RESERVATION)
                    .quantity(item.getQuantity())
                    .referenceId(saved.getId())
                    .reason("Stock reserved for order: " + orderId)
                    .build();
            movementRepository.saveAndFlush(movement);
        }
        log.info("Stock reserved successfully: orderId={}, items={}, expiresAt={}", orderId, reservations.size(), expiresAt);
        return buildResponse(orderId, ReservationStatus.RESERVED, reservations);
    }

    @Override
    @Transactional
    public void release( UUID reservationId ) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if ( reservation.getStatus() != ReservationStatus.RESERVED ) {
            return;
        }
        if ( isExpired(reservation) ) {
            expireReservation(reservation);
            return;
        }
        int updatedRows = inventoryRepository.releaseStock(reservation.getProductId(), reservation.getQuantity());
        if ( updatedRows == 0 ) {
            throw new IllegalStateException("Unable to release reserved stock: " + reservationId);
        }
        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.saveAndFlush(reservation);
        saveReleaseMovement(reservation);
        log.info("Stock reservation released: reservationId={}, orderId={}", reservationId, reservation.getOrderId());
    }

    @Override
    @Transactional
    public void consume( UUID reservationId ) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if ( reservation.getStatus() != ReservationStatus.RESERVED ) {
            return;
        }
        if ( isExpired(reservation) ) {
            expireReservation(reservation);
            throw new IllegalStateException("Reservation has expired: " + reservationId);
        }
        int updatedRows = inventoryRepository.consumeStock(reservation.getProductId(), reservation.getQuantity());
        if ( updatedRows == 0 ) {
            throw new IllegalStateException("Unable to consume reserved stock: " + reservationId);
        }
        reservation.setStatus(ReservationStatus.CONSUMED);
        reservationRepository.saveAndFlush(reservation);
        StockMovement movement = StockMovement.builder()
                .productId(reservation.getProductId())
                .type(StockMovementType.STOCK_OUT)
                .quantity(reservation.getQuantity())
                .referenceId(reservationId)
                .reason("Stock consumed for order: " + reservation.getOrderId())
                .build();
        movementRepository.saveAndFlush(movement);
        log.info("Stock reservation consumed: reservationId={}, orderId={}", reservationId, reservation.getOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public StockReservationResponse getById( UUID reservationId ) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        return buildResponse(reservation.getOrderId(), toResponseStatus(reservation.getStatus()), List.of(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public StockReservationResponse getByOrderId( UUID orderId ) {
        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        if ( reservations.isEmpty() ) {
            throw new ReservationNotFoundException(orderId);
        }
        ReservationStatus status = toResponseStatus(reservations.getFirst()
                .getStatus());
        return buildResponse(orderId, status, reservations);
    }

    @Override
    @Transactional
    public void expireReservations() {
        Instant now = Instant.now();
        List<StockReservation> expiredReservations = reservationRepository.findExpiredReservations(now);
        for ( StockReservation reservation : expiredReservations ) {
            expireReservation(reservation);
            log.info("Reservation expired: reservationId={}, orderId={}, productId={}", reservation.getId(), reservation.getOrderId(), reservation.getProductId());
        }
    }

    @Transactional
    protected void expireReservation( StockReservation reservation ) {
        if ( reservation.getStatus() != ReservationStatus.RESERVED ) {
            return;
        }
        int updatedRows = inventoryRepository.releaseStock(reservation.getProductId(), reservation.getQuantity());
        if ( updatedRows == 0 ) {
            throw new IllegalStateException("Unable to release expired stock: " + reservation.getId());
        }
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.saveAndFlush(reservation);
        StockMovement movement = StockMovement.builder()
                .productId(reservation.getProductId())
                .type(StockMovementType.RELEASE)
                .quantity(reservation.getQuantity())
                .referenceId(reservation.getId())
                .reason("Reservation expired for order: " + reservation.getOrderId())
                .build();
        movementRepository.saveAndFlush(movement);
    }

    private boolean isExpired( StockReservation reservation ) {
        return reservation.getExpiresAt() != null && !reservation.getExpiresAt()
                .isAfter(Instant.now());
    }

    private void validateDuplicateProducts( ReserveStockRequest request ) {
        Set<UUID> productIds = new HashSet<>();
        for ( ReserveStockRequest.ReservationItem item : request.getItems() ) {
            if ( !productIds.add(item.getProductId()) ) {
                throw new IllegalArgumentException("Duplicate product in reservation request: " + item.getProductId());
            }
        }
    }

    private void saveReleaseMovement( StockReservation reservation ) {
        StockMovement movement = StockMovement.builder()
                .productId(reservation.getProductId())
                .type(StockMovementType.RELEASE)
                .quantity(reservation.getQuantity())
                .referenceId(reservation.getId())
                .reason("Reservation released for order: " + reservation.getOrderId())
                .build();
        movementRepository.saveAndFlush(movement);
    }

    private ReservationStatus toResponseStatus( ReservationStatus status ) {
        return switch ( status ) {
            case RESERVED -> ReservationStatus.RESERVED;
            case CONSUMED -> ReservationStatus.CONSUMED;
            case RELEASED -> ReservationStatus.RELEASED;
            case EXPIRED -> ReservationStatus.EXPIRED;
            case FAILED -> ReservationStatus.FAILED;
        };
    }

    private StockReservationResponse buildResponse( UUID orderId, ReservationStatus status, List<StockReservation> reservations ) {
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
                .createdAt(reservations.stream()
                        .map(StockReservation::getCreatedAt)
                        .min(Comparator.naturalOrder())
                        .orElse(null))
                .build();
    }
}