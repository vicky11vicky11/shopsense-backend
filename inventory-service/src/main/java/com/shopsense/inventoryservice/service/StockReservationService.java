package com.shopsense.inventoryservice.service;

import com.shopsense.inventoryservice.request.ReserveStockRequest;
import com.shopsense.inventoryservice.response.StockReservationResponse;

import java.util.UUID;

public interface StockReservationService {

    StockReservationResponse reserve( ReserveStockRequest request );

    void release( UUID reservationId );

    void consume( UUID reservationId );
}